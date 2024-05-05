package com.memo.game.controller;

import com.memo.game.dto.IndexRequest;
import com.memo.game.dto.StartSinglePlayerRequest;
import com.memo.game.model.SinglePlayer;
import com.memo.game.service.MemoSingleGameService;
import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class SinglePlayerController {
    private final MemoSingleGameService memoSingleGameService;
    private final TokenService tokenService;
    @Autowired
    public SinglePlayerController(MemoSingleGameService memoSingleGameService,
                                  TokenService tokenService) {
        this.memoSingleGameService = memoSingleGameService;
        this.tokenService = tokenService;
    }
    @PostMapping("/api/singlePlayer/startSinglePlayer")
    public ResponseEntity<?> startGame(HttpServletRequest request,
    @RequestBody StartSinglePlayerRequest startSinglePlayerRequest) {

        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int initialTime = startSinglePlayerRequest.getInitialTime();
        int numberOfPairs = startSinglePlayerRequest.getNumberOfPairs();
        SinglePlayer singleplayer = new SinglePlayer(numberOfPairs, initialTime, memoSingleGameService);

        UUID userId = tokenService.extractUserIdFromToken(token);
        if(userId==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        String sessionId = singleplayer.getPlayId().toString();
        memoSingleGameService.addSinglePlayerToList(singleplayer, userId);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sessionId", sessionId);
        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/api/singlePlayer/getRemainingTime/{sessionId}")
    public ResponseEntity<?> getRemainingTime(HttpServletRequest request,
        @PathVariable String sessionId) {

        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SinglePlayer singleplayer = memoSingleGameService.getSinglePlayerByGameIdFromList(UUID.fromString(sessionId));
        if (singleplayer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found!");
        }

        if(singleplayer.isGameOver()) {
            memoSingleGameService.removeSinglePlayerFromList(singleplayer);
        }

        int remainingTime = singleplayer.getTimeRemaining();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("remainingTime", remainingTime);
        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/api/singlePlayer/getCard/{sessionId}")
    public ResponseEntity<?> getTwoCards(HttpServletRequest request,
    @PathVariable String sessionId, @RequestBody IndexRequest indexRequest) {

        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SinglePlayer singleplayer = memoSingleGameService.getSinglePlayerByGameIdFromList(UUID.fromString(sessionId));
        if (singleplayer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found!");
        }

        int index = indexRequest.getIndex();

        Map<Integer, Integer> cards = singleplayer.getCard(index);
        if(singleplayer.isGameOver()) {
            memoSingleGameService.removeSinglePlayerFromList(singleplayer);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("cards", cards);
        responseMap.put("equals", singleplayer.getArePreviousCardsequal());
        responseMap.put("ended", singleplayer.isGameOver());
        responseMap.put("won", singleplayer.getWon());
        responseMap.put("guessedBoard", singleplayer.getGuessedBoard());
        responseMap.put("remainingTime", singleplayer.getTimeRemaining());
        responseMap.put("numOfGuessed", singleplayer.getNumOfGuessedPairs());

        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/api/singlePlayer/leaveGame/{sessionId}")
    public ResponseEntity<?> leaveGame(HttpServletRequest request,
                                              @PathVariable String sessionId) {

        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SinglePlayer singleplayer = memoSingleGameService.getSinglePlayerByGameIdFromList(UUID.fromString(sessionId));
        if (singleplayer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found!");
        }

        singleplayer.leaveGame();
        memoSingleGameService.removeSinglePlayerFromList(singleplayer);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/singlePlayer/isPlayValid/{sessionId}")
    public ResponseEntity<?> isPlayValid(HttpServletRequest request,
                                       @PathVariable String sessionId) {
        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SinglePlayer singleplayer = memoSingleGameService.getSinglePlayerByGameIdFromList(UUID.fromString(sessionId));
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