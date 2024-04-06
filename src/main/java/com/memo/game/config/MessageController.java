package com.memo.game.config;

import com.memo.game.dto.JoinMessage;
import com.memo.game.dto.MultiPlayerMessage;
import com.memo.game.dto.PlayerMessage;
import com.memo.game.model.MultiPlayer;
import com.memo.game.service.MultiPlayerService;
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

    /**
     * Manager for the Tic-Tac-Toe games.
     */
    private final MultiPlayerService multiPlayerService = new MultiPlayerService();

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
        MultiPlayer game = multiPlayerService.joinGame(message.getPlayer(), message.getNumOfPairs());
        if (game == null) {
            MultiPlayerMessage errorMessage = new MultiPlayerMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Cannot join");
            return errorMessage;
        }
        headerAccessor.getSessionAttributes().put("gameId", game.getPlayId());
        headerAccessor.getSessionAttributes().put("player", message.getPlayer());

        MultiPlayerMessage gameMessage = gameToMessage(game);
        gameMessage.setType("game.joined");
        return gameMessage;
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
        MultiPlayer game = multiPlayerService.leaveGame(message.getPlayer());
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
        UUID player = message.getSender();
        UUID gameId = message.getGameId();
        int index = message.getIndex();
        MultiPlayer game = multiPlayerService.getGame(gameId);

        if (game == null || game.isGameOver()) {
            MultiPlayerMessage errorMessage = new MultiPlayerMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Game not found or is already over.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameId, errorMessage);
            return;
        }

        if (!game.isGameStarted()) {
            MultiPlayerMessage errorMessage = new MultiPlayerMessage();
            errorMessage.setType("error");
            errorMessage.setContent("Game is waiting for another player to join.");
            this.messagingTemplate.convertAndSend("/topic/game." + gameId, errorMessage);
            return;
        }

        if (game.isPlayersTurn(player)) {
            game.getCard(player, index);

            MultiPlayerMessage gameStateMessage = new MultiPlayerMessage(game);
            gameStateMessage.setType("game.move");
            this.messagingTemplate.convertAndSend("/topic/game." + gameId, gameStateMessage);

            if (game.isGameOver()) {
                MultiPlayerMessage gameOverMessage = gameToMessage(game);
                gameOverMessage.setType("game.gameOver");
                this.messagingTemplate.convertAndSend("/topic/game." + gameId, gameOverMessage);
                multiPlayerService.removeGame(gameId);
            }
        }
    }

    /*@EventListener
    public void SessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String gameId = headerAccessor.getSessionAttributes().get("gameId").toString();
        String player = headerAccessor.getSessionAttributes().get("player").toString();
        TicTacToe game = ticTacToeManager.getGame(gameId);
        if (game != null) {
            if (game.getPlayer1().equals(player)) {
                game.setPlayer1(null);
                if (game.getPlayer2() != null) {
                    game.setGameState(GameState.PLAYER2_WON);
                    game.setWinner(game.getPlayer2());
                } else {
                    ticTacToeManager.removeGame(gameId);
                }
            } else if (game.getPlayer2() != null && game.getPlayer2().equals(player)) {
                game.setPlayer2(null);
                if (game.getPlayer1() != null) {
                    game.setGameState(GameState.PLAYER1_WON);
                    game.setWinner(game.getPlayer1());
                } else {
                    ticTacToeManager.removeGame(gameId);
                }
            }
            TicTacToeMessage gameMessage = gameToMessage(game);
            gameMessage.setType("game.gameOver");
            messagingTemplate.convertAndSend("/topic/game." + gameId, gameMessage);
            ticTacToeManager.removeGame(gameId);
        }
    }*/

    private MultiPlayerMessage gameToMessage(MultiPlayer game) {
        MultiPlayerMessage message = new MultiPlayerMessage();
        message.setGameId(game.getPlayId());
        message.setPlayer1(game.getPlayer1Id());
        message.setPlayer2(game.getPlayer2Id());
        message.setBoard(game.getGuessedBoard());
        message.setTurn(game.isPlayer1sTurn());
        message.setWinner(game.getWinner());
        message.setGameStarted(game.isGameStarted());
        message.setGameOver(game.isGameOver());
        return message;
    }
}