package com.memo.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memo.game.entity.MemoSingleGame;
import com.memo.game.model.SinglePlayer;
import com.memo.game.service.MemoSingleGameService;
import com.memo.game.service.MultiPlayerStatService;
import com.memo.game.service.TokenBlacklistService;
import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class StatisticsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MemoSingleGameService memoSingleGameService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private MultiPlayerStatService multiPlayerStatService;
    private final String token = "token123";
    private final String invalidToken = "invalid_token";
    private final String noUserToken = "no_user";
    private final UUID userId = UUID.randomUUID();
    private final List<MemoSingleGame> gamesList = new ArrayList<>();
    @BeforeEach
    public void setUp() {
        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        when(tokenBlacklistService.addToBlacklist(any())).thenReturn(false);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.isTokenValid(noUserToken)).thenReturn(true);
        when(tokenService.isTokenValid(invalidToken)).thenReturn(false);
        when(tokenService.extractUserIdFromToken(token)).thenReturn(userId);
        when(tokenService.extractUserIdFromToken(noUserToken)).thenReturn(null);
        boolean won = true;
        for(int i=0; i<20; i++) {
            won=!won;
            gamesList.add(new MemoSingleGame(
                    UUID.randomUUID(),
                    userId,
                    won,
                    i*10,
                    8,
                    1000
            ));
        }
    }

    @Test
    public void getAllSpStats() throws Exception {
        int totalItems = gamesList.size();
        int totalPages = gamesList.size()/10;
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(memoSingleGameService.getTotalGamesCountByUserIdFromDb(any(UUID.class))).thenReturn(totalItems);
        when(memoSingleGameService.findGamesByUserIdInDb(any(UUID.class), anyInt(), anyInt())).thenReturn(gamesList);

        mockMvc.perform(post("/api/singlePlayerStatistics/all")
                        .header("Authorization", "Bearer valid-token")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalItems").value(totalItems))
                .andExpect(jsonPath("$.totalPages").value(totalPages))
                .andExpect(jsonPath("$.data", hasSize(gamesList.size())));
    }

    @Test
    public void getAllSpStatsUnauthorizedAccess() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(invalidToken);
        mockMvc.perform(post("/api/singlePlayerStatistics/all")
                        .header("Authorization", "Bearer " + invalidToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAllSpStatsUserNotFound() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(noUserToken);
        mockMvc.perform(post("/api/singlePlayerStatistics/all")
                        .header("Authorization", "Bearer " + noUserToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found!"));
    }

    @Test
    public void getAllSpStatsIncorrectPageParameter() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        mockMvc.perform(post("/api/singlePlayerStatistics/all")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect param: page"));
    }

    @Test
    public void getAllSpStatsIncorrectSizeParameter() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        mockMvc.perform(post("/api/singlePlayerStatistics/all")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect param: size"));
    }

    @Test
    public void validRequestSummarized() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(memoSingleGameService.findGamesByUserIdInDb(any(UUID.class))).thenReturn(gamesList);

        mockMvc.perform(post("/api/singlePlayerStatistics/summarized")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pairs").value(8))
                .andExpect(jsonPath("$[0].time").value(1000))
                .andExpect(jsonPath("$[0].losses").value(10))
                .andExpect(jsonPath("$[0].wins").value(10))
                .andExpect(jsonPath("$[0].winningRate").value(50))
                .andExpect(jsonPath("$[0].avgRemainingTime").value(100))
                .andExpect(jsonPath("$[0].numOfGames").value(20));
    }

    @Test
    public void validRequestMultiPlayerStatistics() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);

        List<HashMap<String, Object>> leaderboard = new ArrayList<>();
        HashMap<String, Object> player1 = new HashMap<>();
        player1.put("userName", "Player 1");
        player1.put("wins", 20);
        player1.put("rank", 1);
        leaderboard.add(player1);
        HashMap<String, Object> player2 = new HashMap<>();
        player2.put("userName", "Player 2");
        player2.put("wins", 10);
        player2.put("rank", 2);
        leaderboard.add(player2);

        when(multiPlayerStatService.getLeaderBoard(anyInt())).thenReturn(leaderboard);

        mockMvc.perform(post("/api/multiPlayerStatistics")
                        .header("Authorization", "Bearer " + token)
                        .param("pairs", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userName").value("Player 1"))
                .andExpect(jsonPath("$[0].wins").value(20))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[1].userName").value("Player 2"))
                .andExpect(jsonPath("$[1].wins").value(10))
                .andExpect(jsonPath("$[1].rank").value(2));
    }

    @Test
    public void invalidPairsParamMultiPlayerStatistics() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        mockMvc.perform(post("/api/multiPlayerStatistics")
                        .header("Authorization", "Bearer " + token)
                        .param("pairs", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect param: page"));
    }
}
