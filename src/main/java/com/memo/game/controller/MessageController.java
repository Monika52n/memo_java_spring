package com.memo.game.controller;

import com.memo.game.dto.JoinMessage;
import com.memo.game.dto.MultiPlayerMessage;
import com.memo.game.dto.PlayerMessage;
import com.memo.game.model.MultiPlayer;
import com.memo.game.service.MemoUsersService;
import com.memo.game.service.MultiPlayerService;
import com.memo.game.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Controller class for handling WebSocket messages and managing the Tic-Tac-Toe games.
 *
 * @author Joabson Arley do Nascimento
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
    private MemoUsersService memoUsersService;

    /**
     * Manager for the Tic-Tac-Toe games.
     */
    @Autowired
    private MultiPlayerService multiPlayerService;

    /**
     * Handles a request from a client to join a Tic-Tac-Toe game.
     * If a game is available and the player is successfully added to the game,
     * the current state of the game is sent to all subscribers of the game's topic.
     *
     * @param message the message from the client containing the player's name
     * @return the current state of the game, or an error message if the player was unable to join
     */
    @MessageMapping("/game.join")
    @SendTo("/topic/game.state")
    public Object joinGame(@Payload JoinMessage message, SimpMessageHeaderAccessor headerAccessor) {
        MultiPlayerMessage responseMessage;
        if (!tokenService.isTokenValid(message.getToken())) {
            responseMessage = new MultiPlayerMessage(memoUsersService);
            responseMessage.setType("error");
            responseMessage.setType("Unauthorized");
            return responseMessage;
        }
        UUID playerId = tokenService.extractUserIdFromToken(message.getToken());
        if(playerId==null) {
            MultiPlayerMessage errorMessage = new MultiPlayerMessage(memoUsersService);
            errorMessage.setType("error");
            errorMessage.setContent("Invalid token");
            return errorMessage;
        }

        MultiPlayer game;
        UUID gameId = null;
        if(message.getFriendRoomId()!=null) {
            gameId = UUID.fromString(message.getFriendRoomId());
        }
        if(message.isWantToPlayWithFriend()) {
            game = multiPlayerService.joinGameWithFriend(playerId, message.getNumOfPairs(), gameId);
        } else {
            game = multiPlayerService.joinGame(playerId, message.getNumOfPairs());
        }

        if (game == null || game.getPlayId()==null) {
            responseMessage = new MultiPlayerMessage(memoUsersService);
            responseMessage.setType("error");
            responseMessage.setContent("Cannot join");
            return responseMessage;
        }

        headerAccessor.getSessionAttributes().put("gameId", game.getPlayId());
        headerAccessor.getSessionAttributes().put("player", playerId);

        responseMessage = gameToMessage(game);
        responseMessage.setType("game.joined");
        responseMessage.setSender(message.getToken());
        return responseMessage;
    }

    /**
     * Handles a request from a client to leave a Tic-Tac-Toe game.
     * If the player is successfully removed from the game, a message is sent to subscribers
     * of the game's topic indicating that the player has left.
     *
     * @param message the message from the client containing the player's name
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
     * Handles a request from a client to make a move in a Tic-Tac-Toe game.
     * If the move is valid, the game state is updated and sent to all subscribers of the game's topic.
     * If the game is over, a message is sent indicating the result of the game.
     *
     * @param message the message from the client containing the player's name, game ID, and move
     */
    @MessageMapping("/game.move")
    public void makeMove(@Payload MultiPlayerMessage message) {
        String token = message.getSenderToken();
        UUID gameId = message.getGameId();
        int index = message.getIndex();
        MultiPlayer game = multiPlayerService.getGame(gameId);

        if (!tokenService.isTokenValid(token)) {
            MultiPlayerMessage errorMessage = new MultiPlayerMessage(memoUsersService);
            errorMessage.setType("error");
            this.messagingTemplate.convertAndSend("/topic/game." + gameId, errorMessage);
            errorMessage.setType("Unauthorized");
            return;
        }

        UUID player = tokenService.extractUserIdFromToken(token);
        if(player==null) {
            MultiPlayerMessage errorMessage = new MultiPlayerMessage(memoUsersService);
            errorMessage.setType("error");
            errorMessage.setContent("Invalid token.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameId, errorMessage);
            return;
        }
        if (game == null) {
            MultiPlayerMessage errorMessage = new MultiPlayerMessage(memoUsersService);
            errorMessage.setType("error");
            errorMessage.setContent("Game not found or is already over.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameId, errorMessage);
            return;
        }

        if (!game.isGameStarted()) {
            MultiPlayerMessage errorMessage = new MultiPlayerMessage(memoUsersService);
            errorMessage.setType("error");
            errorMessage.setContent("Game is waiting for another player to join.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameId, errorMessage);
            return;
        }

        if (game.isPlayersTurn(player)) {
            Map<Integer, Integer> lastMove = game.getCard(player, index);

            MultiPlayerMessage gameStateMessage = new MultiPlayerMessage(game, memoUsersService);
            gameStateMessage.setType("game.move");
            gameStateMessage.setLastMove(lastMove);
            this.messagingTemplate.convertAndSend("/topic/game." + gameId, gameStateMessage);

            if (game.isGameOver()) {
                MultiPlayerMessage gameOverMessage = gameToMessage(game);
                gameOverMessage.setType("game.gameOver");
                multiPlayerService.saveGame(game);
                this.messagingTemplate.convertAndSend("/topic/game." + gameId, gameOverMessage);
                multiPlayerService.removeGame(gameId);
            }
        }
    }

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

    private MultiPlayerMessage gameToMessage(MultiPlayer game) {
        MultiPlayerMessage message = new MultiPlayerMessage(memoUsersService);
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
}