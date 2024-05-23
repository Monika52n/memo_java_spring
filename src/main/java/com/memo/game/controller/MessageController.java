package com.memo.game.controller;

import com.memo.game.dto.JoinMessage;
import com.memo.game.dto.MultiPlayerMessage;
import com.memo.game.dto.PlayerMessage;
import com.memo.game.gameModel.MultiPlayer;
import com.memo.game.service.UserService;
import com.memo.game.service.MultiPlayerService;
import com.memo.game.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller class for managing WebSocket messages and multiplayer games.
 * Handles WebSocket communication between clients for real-time multiplayer game interactions,
 * such as game moves, and game state updates.
 * Manages multiplayer game sessions, including game initialization, player connections,
 * and game termination.
 */
@Controller
public class MessageController {

    /**
     * Template for sending messages to clients through the message broker.
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private MultiPlayerService multiPlayerService;

    /**
     * Creates an error message to be sent to clients over WebSocket in case of errors during multiplayer game interactions.
     *
     * @param content   The content of the error message.
     * @param playerId  The UUID of the player associated with the error, if applicable.
     * @return The MultiPlayerMessage representing the error message.
     */
    private MultiPlayerMessage createErrorMessage(String content, UUID playerId) {
        MultiPlayerMessage responseMessage = new MultiPlayerMessage(userService);
        responseMessage.setType("error");
        responseMessage.setContent(content);
        responseMessage.setPlayer1(playerId);
        return responseMessage;
    }

    /**
     * Handles a request from a client to join a MultiPlayer game.
     * If a game is available and the player is successfully added to the game,
     * the current state of the game is sent to all subscribers of the game's topic.
     *
     * @param message the message from the client containing the player's token, number of pairs for the game,
     *                and weather the player wants to play with a friend, and the code of their friend's game
     * @return the current state of the game, or an error message if the player was unable to join
     */
    @MessageMapping("/game.join")
    @SendTo("/topic/game.state")
    public MultiPlayerMessage joinGame(@Payload JoinMessage message, SimpMessageHeaderAccessor headerAccessor) {
        if (!tokenService.isTokenValid(message.getToken())) {
            return createErrorMessage("Unauthorized", null);
        }
        UUID playerId = tokenService.extractUserIdFromToken(message.getToken());
        if(playerId==null) {
            return createErrorMessage("User not found", null);
        }

        MultiPlayer game;

        if(message.isWantToPlayWithFriend()) {
            UUID gameId = null;
            if(message.getFriendRoomId()!=null) {
                try {
                    gameId = UUID.fromString(message.getFriendRoomId());
                }
                catch(IllegalArgumentException e) {
                    if(message.getNumOfPairs()<=0) {
                        return createErrorMessage("Incorrect params", playerId);
                    }
                }
            }
            game = multiPlayerService.joinGameWithFriend(playerId, message.getNumOfPairs(), gameId);
        } else {
            if(message.getNumOfPairs()<=0) {
                return createErrorMessage("Incorrect params", playerId);
            }
            game = multiPlayerService.joinGame(playerId, message.getNumOfPairs());
        }

        if (game == null || game.getPlayId()==null) {
            return createErrorMessage("Cannot join", playerId);
        }

        headerAccessor.getSessionAttributes().put("gameId", game.getPlayId());
        headerAccessor.getSessionAttributes().put("player", playerId);

        MultiPlayerMessage responseMessage;
        responseMessage = gameToMessage(game);
        responseMessage.setType("game.joined");
        responseMessage.setSender(message.getToken());
        return responseMessage;
    }

    /**
     * Handles a request from a client to leave a MultiPlayer game.
     * If the player is successfully removed from the game, a message is sent to subscribers
     * of the game's topic indicating that the player has left.
     *
     * @param message the message from the client containing the player's token
     */
    @MessageMapping("/game.leave")
    public void leaveGame(@Payload PlayerMessage message) {
        UUID playerId = tokenService.extractUserIdFromToken(message.getToken());
        MultiPlayer game = multiPlayerService.leaveGame(playerId);
        if (game != null) {
            MultiPlayerMessage gameMessage = gameToMessage(game);
            gameMessage.setType("game.left");
            messagingTemplate.convertAndSend("/topic/game." + (game.getPlayId()).toString(), gameMessage);
        }
    }

    /**
     * Handles a request from a client to make a move in a MultiPlayer game.
     * If the move is valid, the game state is updated and sent to all subscribers of the game's topic.
     * If the game is over, a message is sent indicating the result of the game.
     *
     * @param message the message from the client containing the player's token, game ID, and index of the card
     */
    @MessageMapping("/game.move")
    public void makeMove(@Payload MultiPlayerMessage message) {
        String token = message.getSenderToken();
        UUID gameId = message.getGameId();
        int index = message.getIndex();
        MultiPlayer game = multiPlayerService.getGame(gameId);

        if (!tokenService.isTokenValid(token)) {
            this.messagingTemplate.convertAndSend("/topic/game." + gameId,
                    createErrorMessage("Unauthorized", null));
            return;
        }
        UUID player = tokenService.extractUserIdFromToken(token);
        if(player==null) {
            this.messagingTemplate.convertAndSend("/topic/game." + gameId,
                    createErrorMessage("Invalid token", null));
            return;
        }
        if (game == null) {
            this.messagingTemplate.convertAndSend("/topic/game." + gameId,
                    createErrorMessage("Game not found or is already over.", player));
            return;
        }
        if (!game.isGameStarted()) {
            this.messagingTemplate.convertAndSend("/topic/game." + gameId,
                    createErrorMessage("Game is waiting for another player to join.", player));
            return;
        }
        if (!game.isPlayersTurn(player)) {
            this.messagingTemplate.convertAndSend("/topic/game." + gameId,
                    createErrorMessage("Not your turn", player));
            return;
        }

        Map<Integer, Integer> lastMove = new HashMap<>();
        try {
            lastMove = game.flipCard(player, index);
        } catch (Exception e) {
            this.messagingTemplate.convertAndSend("/topic/game." + gameId,
                    createErrorMessage("Incorrect params", player));
            return;
        }

        MultiPlayerMessage gameStateMessage = new MultiPlayerMessage(game, userService);
        gameStateMessage.setType("game.move");
        gameStateMessage.setLastMove(lastMove);

        if (game.isGameOver()) {
            gameStateMessage.setType("game.gameOver");
            multiPlayerService.saveGame(game);
            multiPlayerService.removeGame(gameId);
        }
        this.messagingTemplate.convertAndSend("/topic/game." + gameId, gameStateMessage);
    }

    /**
     * Listens for WebSocket session disconnect events. When a player disconnects from a game session,
     * this method handles the cleanup process for the associated multiplayer game. It retrieves the game ID
     * and player UUID from the session attributes, obtains the corresponding multiplayer game from the
     * {@code MultiPlayerService}, removes the disconnected player from the game, sends a game over message
     * to all subscribers of the game's topic, and finally removes the game from the service.
     *
     * @param event The event indicating that a WebSocket session has been disconnected.
     */
    @EventListener
    public void SessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        UUID gameId = (UUID) headerAccessor.getSessionAttributes().get("gameId");
        UUID player = (UUID) headerAccessor.getSessionAttributes().get("player");
        MultiPlayer game = multiPlayerService.getGame(gameId);

        if (game != null) {
            game.playerLeaves(player);
            MultiPlayerMessage gameMessage = gameToMessage(game);
            gameMessage.setType("game.gameOver");
            messagingTemplate.convertAndSend("/topic/game." + gameId, gameMessage);
            multiPlayerService.removeGame(gameId);
        }
    }

    /**
     * Converts a MultiPlayer game object into a MultiPlayerMessage object for sending game state updates
     * and information to clients over WebSocket.
     *
     * @param game The MultiPlayer game object to be converted into a message.
     * @return The MultiPlayerMessage containing the game state and information.
     */
    private MultiPlayerMessage gameToMessage(MultiPlayer game) {
        MultiPlayerMessage message = new MultiPlayerMessage(userService);
        message.setGameId(game.getPlayId());
        message.setPlayer1(game.getPlayer1Id());
        message.setPlayer2(game.getPlayer2Id());
        message.setBoard(game.getGuessedBoard());
        message.setTurn(game.isPlayer1sTurn());
        message.setWinner(game.getWinner());
        message.setGameStarted(game.isGameStarted());
        message.setGameOver(game.isGameOver());
        message.setPlayer1GuessedCards(game.getPlayer1GuessedCards());
        message.setPlayer2GuessedCards(game.getPlayer2GuessedCards());
        return message;
    }

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public void setMemoUsersService(UserService userService) {
        this.userService = userService;
    }

    public void setMultiPlayerService(MultiPlayerService multiPlayerService) {
        this.multiPlayerService = multiPlayerService;
    }

    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
}