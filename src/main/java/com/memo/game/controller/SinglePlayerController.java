package com.memo.game.controller;

import com.memo.game.dto.IndexRequest;
import com.memo.game.dto.StartSinglePlayerRequest;
import com.memo.game.gameModel.SinglePlayer;
import com.memo.game.service.SinglePlayerService;
import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for managing single-player game sessions.
 * Handles HTTP requests related to single-player games, such as starting a new game, retrieving game status,
 * and interacting with the game board.
 * Enables communication with the front end, allowing players to play single-player memory matching games.
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class SinglePlayerController {
    private final SinglePlayerService singlePlayerService;
    private final TokenService tokenService;
    @Autowired
    public SinglePlayerController(SinglePlayerService singlePlayerService,
                                  TokenService tokenService) {
        this.singlePlayerService = singlePlayerService;
        this.tokenService = tokenService;
    }

    /**
     * Starts a new single-player game session.
     *
     * This method handles a request to start a single-player game. It extracts the player's token
     * from the request, validates it, and then initializes a new single-player game with the specified
     * number of pairs and initial time. If the token is invalid or required parameters are missing,
     * it returns the appropriate error response.
     *
     * @param request the HTTP request containing the player's token
     * @param startSinglePlayerRequest the request body containing the initial time and number of pairs for the game
     * @return a ResponseEntity containing the session ID of the new game or an error message if the game could not be started
    */
    @PostMapping("/api/singlePlayer/startSinglePlayer")
    public ResponseEntity<?> startGame(HttpServletRequest request,
    @RequestBody StartSinglePlayerRequest startSinglePlayerRequest) {

        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int initialTime = startSinglePlayerRequest.getInitialTime();
        int numberOfPairs = startSinglePlayerRequest.getNumberOfPairs();
        if (initialTime==0 || numberOfPairs==0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SinglePlayer singleplayer = new SinglePlayer(numberOfPairs, initialTime, singlePlayerService);

        UUID userId = tokenService.extractUserIdFromToken(token);
        if(userId==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        String sessionId = singleplayer.getPlayId().toString();
        singlePlayerService.addSinglePlayerToList(singleplayer, userId);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sessionId", sessionId);
        return ResponseEntity.ok(responseMap);
    }

    /**
     * Retrieves the remaining time for a single-player game session.
     *
     * This method handles a request to get the remaining time for a specific single-player game session.
     * It extracts the player's token from the request, validates it, and then retrieves the game session
     * using the provided session ID. If the session is found and the game is not over, it returns the remaining time.
     * If the game is over, the session is removed from the list.
     *
     * @param request the HTTP request containing the player's token
     * @param sessionId the ID of the game session
     * @return a ResponseEntity containing the remaining time for the session or an error message if the session could not be found
    */
    @PostMapping("/api/singlePlayer/getRemainingTime/{sessionId}")
    public ResponseEntity<?> getRemainingTime(HttpServletRequest request,
        @PathVariable String sessionId) {

        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SinglePlayer singleplayer = singlePlayerService.getSinglePlayerByGameIdFromList(UUID.fromString(sessionId));
        if (singleplayer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found!");
        }

        if(singleplayer.isGameOver()) {
            singlePlayerService.removeSinglePlayerFromList(singleplayer);
        }

        int remainingTime = singleplayer.getTimeRemaining();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("remainingTime", remainingTime);
        return ResponseEntity.ok(responseMap);
    }

    /**
     * Flips a card at the specified index in a single-player game session.
     *
     * This method handles a request to flip a card at a specific index for a single-player game session.
     * It extracts the player's token from the request, validates it, and then retrieves the game session
     * using the provided session ID. If the session is found, it flips the card at the specified index.
     * If the game is over, the session is removed from the list.
     *
     * @param request the HTTP request containing the player's token
     * @param sessionId the ID of the game session
     * @param indexRequest the request body containing the index of the card to flip
     * @return a ResponseEntity containing the flipped cards and game state information, or an error message if the session could not be found or the index is invalid
     */
    @PostMapping("/api/singlePlayer/getCard/{sessionId}")
    public ResponseEntity<?> flipCard(HttpServletRequest request,
    @PathVariable String sessionId, @RequestBody IndexRequest indexRequest) {

        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SinglePlayer singleplayer = singlePlayerService.getSinglePlayerByGameIdFromList(UUID.fromString(sessionId));
        if (singleplayer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found!");
        }

        int index = indexRequest.getIndex();
        Map<Integer, Integer> cards;
        try {
            cards = singleplayer.flipCard(index);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if(singleplayer.isGameOver()) {
            singlePlayerService.removeSinglePlayerFromList(singleplayer);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("cards", cards);
        responseMap.put("equals", singleplayer.getArePreviousCardsEqual());
        responseMap.put("ended", singleplayer.isGameOver());
        responseMap.put("won", singleplayer.getWon());
        responseMap.put("guessedBoard", singleplayer.getGuessedBoard());
        responseMap.put("remainingTime", singleplayer.getTimeRemaining());
        responseMap.put("numOfGuessed", singleplayer.getNumOfGuessedPairs());

        return ResponseEntity.ok(responseMap);
    }

    /**
     * Allows a player to leave a single-player game session.
     *
     * This method handles a request to leave a single-player game session. It extracts the player's token
     * from the request, validates it, and then retrieves the game session using the provided session ID.
     * If the session is found, the player leaves the game and the session is removed from the list.
     *
     * @param request the HTTP request containing the player's token
     * @param sessionId the ID of the game session
     * @return a ResponseEntity indicating the outcome of the request, with no content if successful,
     *         or an error message if the session could not be found
     */
    @PostMapping("/api/singlePlayer/leaveGame/{sessionId}")
    public ResponseEntity<?> leaveGame(HttpServletRequest request,
                                              @PathVariable String sessionId) {

        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SinglePlayer singleplayer = singlePlayerService.getSinglePlayerByGameIdFromList(UUID.fromString(sessionId));
        if (singleplayer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found!");
        }

        singleplayer.leaveGame();
        singlePlayerService.removeSinglePlayerFromList(singleplayer);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if the current play in a single-player game session is valid.
     *
     * This method handles a request to verify the validity of the current play for a single-player game session.
     * It extracts the player's token from the request, validates it, and then retrieves the game session
     * using the provided session ID. It returns the validity of the play and additional game state information if the session is valid.
     *
     * @param request the HTTP request containing the player's token
     * @param sessionId the ID of the game session
     * @return a ResponseEntity containing the validity of the play and game state information, or an error message if the token is invalid
     */
    @PostMapping("/api/singlePlayer/isPlayValid/{sessionId}")
    public ResponseEntity<?> isPlayValid(HttpServletRequest request,
                                       @PathVariable String sessionId) {
        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SinglePlayer singleplayer = singlePlayerService.getSinglePlayerByGameIdFromList(UUID.fromString(sessionId));
        Map<String, Object> responseMap = new HashMap<>();
        if (singleplayer == null) {
            responseMap.put("isValid" , false);
        } else {
            responseMap.put("isValid" , true);
            responseMap.put("guessedBoard", singleplayer.getGuessedBoard());
            responseMap.put("remainingTime", singleplayer.getTimeRemaining());
            responseMap.put("numOfGuessed", singleplayer.getNumOfGuessedPairs());
            responseMap.put("cards", singleplayer.getPreviousMove());
        }
        return ResponseEntity.ok(responseMap);
    }
}