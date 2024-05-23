package com.memo.game.controller;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.service.SinglePlayerService;
import com.memo.game.service.MultiPlayerStatService;
import com.memo.game.service.SinglePlayerStatService;
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

/**
 * REST controller for managing game statistics.
 * Handles HTTP requests related to retrieving and presenting game statistics, such as leaderboards,
 * player rankings, and other aggregated data.
 * Facilitates communication with the front end to display statistical information to users.
 */
@RestController
public class StatisticsController {
    private final SinglePlayerService singlePlayerService;
    private final TokenService tokenService;
    private final MultiPlayerStatService multiPlayerStatService;

    @Autowired
    public StatisticsController(SinglePlayerService singlePlayerService, TokenService tokenService, MultiPlayerStatService multiPlayerStatService) {
        this.singlePlayerService = singlePlayerService;
        this.tokenService = tokenService;
        this.multiPlayerStatService = multiPlayerStatService;
    }

    /**
     * Retrieves paginated single-player game statistics for a user.
     *
     * This method handles a request to fetch paginated statistics for single-player games played by the user.
     * It extracts the player's token from the request, validates it, and retrieves the user's ID.
     * It then calculates the pagination parameters and fetches the appropriate game data from the database.
     *
     * @param request the HTTP request containing the player's token
     * @param page the current page number (default is 1)
     * @param size the number of items per page (default is 10)
     * @return a ResponseEntity containing paginated game statistics or an error message if the token is invalid,
     *         the user is not found, or the pagination parameters are incorrect
     */
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

        int totalItems = singlePlayerService.getTotalGamesCountByUserIdFromDb(userId);

        int totalPages;
        if(totalItems % size == 0 && totalItems!=0) {
            totalPages = (totalItems/size);
        } else {
            totalPages = (totalItems/size)+1;
        }

        if(page<=0 || page>totalPages) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect param: page");
        }

        List<MemoSingleGame> responseList = singlePlayerService.findGamesByUserIdInDb(userId, page-1, size);

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("currentPage", page);
        responseMap.put("itemsPerPage", size);
        responseMap.put("totalItems", totalItems);
        responseMap.put("totalPages", totalPages);
        responseMap.put("data", responseList);
        return ResponseEntity.ok(responseMap);
    }

    /**
     * Retrieves summarized statistics for single-player games played by a user.
     *
     * This method handles a request to fetch summarized statistics for single-player games played by the user.
     * It extracts the player's token from the request, validates it, and retrieves the user's ID.
     * It then fetches the list of games played by the user from the database and calculates the summarized statistics.
     *
     * @param request the HTTP request containing the player's token
     * @return a ResponseEntity containing the summarized game statistics or an error message if the token is invalid or the user is not found
     */
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

        List<MemoSingleGame> games = singlePlayerService.findGamesByUserIdInDb(userId);
        SinglePlayerStatService singlePlayerStatService = new SinglePlayerStatService();
        return ResponseEntity.ok(singlePlayerStatService.addList(games));
    }

    /**
     * Retrieves the leaderboard for multiplayer games.
     *
     * This method handles a request to fetch the leaderboard for multiplayer games based on the specified number of pairs.
     * It extracts the player's token from the request and validates it. If the token is valid, it retrieves the leaderboard
     * data from the database based on the specified number of pairs. The default number of pairs is 8 if not provided.
     *
     * @param request the HTTP request containing the player's token
     * @param pairs the number of pairs for which to retrieve the leaderboard (default is 8 if not provided)
     * @return a ResponseEntity containing the leaderboard data or an error message if the token is invalid or the pairs parameter is incorrect
     */
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
