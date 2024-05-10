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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MemoSingleGameService memoSingleGameService;
    @MockBean
    private static TokenService tokenService;
    @MockBean
    private static TokenBlacklistService tokenBlacklistService;
    @MockBean
    private MultiPlayerStatService multiPlayerStatService;
    private static String token;
    private final static UUID userId = UUID.randomUUID();
    private static final List<MemoSingleGame> gamesList = new ArrayList<>();
    @BeforeAll
    public static void setUp() {
        tokenBlacklistService = mock(TokenBlacklistService.class);
        tokenService = mock(TokenService.class);

        token = "token123";
        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        when(tokenBlacklistService.addToBlacklist(any())).thenReturn(false);
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.extractUserIdFromToken(token)).thenReturn(userId);
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
        int totalItems = 20;
        int totalPages = 2;

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
}
