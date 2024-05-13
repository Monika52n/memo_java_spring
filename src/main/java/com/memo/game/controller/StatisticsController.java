package com.memo.game.controller;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.service.MemoSingleGameService;
import com.memo.game.service.MultiPlayerStatService;
import com.memo.game.service.SinglePlayerCreateStatService;
import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.List;

@RestController
public class StatisticsController {
    private final MemoSingleGameService memoSingleGameService;
    private final TokenService tokenService;
    private final MultiPlayerStatService multiPlayerStatService;

    @Autowired
    public StatisticsController(MemoSingleGameService memoSingleGameService, TokenService tokenService, MultiPlayerStatService multiPlayerStatService) {
        this.memoSingleGameService = memoSingleGameService;
        this.tokenService = tokenService;
        this.multiPlayerStatService = multiPlayerStatService;
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
        if(size<=0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect param: size");
        }

        int totalItems = memoSingleGameService.getTotalGamesCountByUserIdFromDb(userId);

        int totalPages;
        if(totalItems % size == 0 && totalItems!=0) {
            totalPages = (totalItems/size);
        } else {
            totalPages = (totalItems/size)+1;
        }

        if(page<=0 || page>totalPages) {
            System.out.println(page + " " + totalPages);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect param: page");
        }

        List<MemoSingleGame> responseList = memoSingleGameService.findGamesByUserIdInDb(userId, page-1, size);

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("currentPage", page);
        responseMap.put("itemsPerPage", size);
        responseMap.put("totalItems", totalItems);
        responseMap.put("totalPages", totalPages);
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

        List<MemoSingleGame> games = memoSingleGameService.findGamesByUserIdInDb(userId);
        SinglePlayerCreateStatService singlePlayerCreateStatService = new SinglePlayerCreateStatService();
        return ResponseEntity.ok(singlePlayerCreateStatService.addList(games));
    }

    @PostMapping("/api/multiPlayerStatistics")
    public ResponseEntity<?> getLeaderBoard(HttpServletRequest request,
                @RequestParam(defaultValue = "8") int pairs) {
        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if(pairs<=0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect param: page");
        }
        List<HashMap<String, Object>> leaderboard = multiPlayerStatService.getLeaderBoard(pairs);
        return ResponseEntity.ok(leaderboard);
    }
}
