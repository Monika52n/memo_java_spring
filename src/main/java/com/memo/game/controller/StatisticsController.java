package com.memo.game.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memo.game.entity.MemoSingleGame;
import com.memo.game.service.MemoSingleGameService;
import com.memo.game.service.SinglePlayerCreateStatService;
import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.*;
import java.util.List;

@RestController
public class StatisticsController {
    private final MemoSingleGameService memoSingleGameService;
    private final TokenService tokenService;

    @Autowired
    public StatisticsController(MemoSingleGameService memoSingleGameService, TokenService tokenService) {
        this.memoSingleGameService = memoSingleGameService;
        this.tokenService = tokenService;
    }

    @PostMapping("/api/singlePlayerStatistics/all")
    public ResponseEntity<?> getSinglePlayerGames(HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = tokenService.extractUserIdFromToken(token);
        if(userId==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        int totalItems = memoSingleGameService.getTotalGamesCountByUserId(userId);
        if(size<=0 || size>totalItems) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect param: size");
        }
        if(page<=0 || page>(totalItems/size)+1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect param: page");
        }

        List<MemoSingleGame> responseList = memoSingleGameService.findGamesByUserId(userId, page-1, size);

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("currentPage", page);
        responseMap.put("itemsPerPage", size);
        responseMap.put("totalItems", totalItems);
        responseMap.put("totalPages", (totalItems/size)+1);
        responseMap.put("data", responseList);
        return ResponseEntity.ok(responseMap);
    }

    @PostMapping("/api/singlePlayerStatistics/summarized")
    public ResponseEntity<?> getSinglePlayerStats(HttpServletRequest request) {
        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = tokenService.extractUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        List<MemoSingleGame> games = memoSingleGameService.findGamesByUserId(userId);
        SinglePlayerCreateStatService singlePlayerCreateStatService
                = new SinglePlayerCreateStatService(games);
        return ResponseEntity.ok(singlePlayerCreateStatService.getList());
    }
}
