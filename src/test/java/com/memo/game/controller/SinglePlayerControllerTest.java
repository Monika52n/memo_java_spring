package com.memo.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memo.game.dto.IndexRequest;
import com.memo.game.dto.StartSinglePlayerRequest;
import com.memo.game.model.SinglePlayer;
import com.memo.game.service.MemoSingleGameService;
import com.memo.game.service.TokenBlacklistService;
import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class SinglePlayerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MemoSingleGameService memoSingleGameService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    private String token;
    private SinglePlayer singlePlayer;
    @BeforeEach
    public void setUp() {
        singlePlayer = new SinglePlayer(8, 60, memoSingleGameService);
        token = "token123";
        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        when(tokenBlacklistService.addToBlacklist(any())).thenReturn(false);
    }

    @Test
    public void startGameTest() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.extractUserIdFromToken(token)).thenReturn(UUID.randomUUID());

        StartSinglePlayerRequest startSinglePlayerRequest = new StartSinglePlayerRequest();
        startSinglePlayerRequest.setInitialTime(300);
        startSinglePlayerRequest.setNumberOfPairs(16);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/startSinglePlayer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startSinglePlayerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").exists());
    }

    @Test
    public void startGameThenUnauthorizedResponse() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(false);

        StartSinglePlayerRequest request = new StartSinglePlayerRequest();
        request.setInitialTime(60);
        request.setNumberOfPairs(8);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/startSinglePlayer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void whenInvalidRequestThenBadRequestResponse() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.extractUserIdFromToken(token)).thenReturn(UUID.randomUUID());
        StartSinglePlayerRequest request = new StartSinglePlayerRequest();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/startSinglePlayer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void startGameWhenUserNotFoundThenNotFoundResponse() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.extractUserIdFromToken(token)).thenReturn(null);

        StartSinglePlayerRequest request = new StartSinglePlayerRequest();
        request.setInitialTime(60);
        request.setNumberOfPairs(8);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/startSinglePlayer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found!")));
    }

    @Test
    public void getRemainingTimeTest() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(memoSingleGameService.getSinglePlayerByGameIdFromList(any(UUID.class))).thenReturn(singlePlayer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/getRemainingTime/{sessionId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingTime").value(singlePlayer.getTimeRemaining()));
    }

    @Test
    public void getRemTimeWhenSessionNotFoundThenNotFoundResponse() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(memoSingleGameService.getSinglePlayerByGameIdFromList(any(UUID.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/getRemainingTime/{sessionId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Session not found!")));
    }

    @Test
    public void getCardsTest() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(memoSingleGameService.getSinglePlayerByGameIdFromList(any(UUID.class))).thenReturn(singlePlayer);

        IndexRequest indexRequest = new IndexRequest();
        indexRequest.setIndex(0);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/getCard/{sessionId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(indexRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").exists())
                .andExpect(jsonPath("$.equals").exists())
                .andExpect(jsonPath("$.ended").exists())
                .andExpect(jsonPath("$.won").exists())
                .andExpect(jsonPath("$.guessedBoard").exists())
                .andExpect(jsonPath("$.remainingTime").exists())
                .andExpect(jsonPath("$.numOfGuessed").exists());
    }

    @Test
    public void getCardWhenInvalidRequestThenBadRequestResponse() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(memoSingleGameService.getSinglePlayerByGameIdFromList(any(UUID.class))).thenReturn(singlePlayer);

        IndexRequest indexRequest = new IndexRequest();
        indexRequest.setIndex(-1);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/getCard/{sessionId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(indexRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void leaveGameThenNoContentReturned() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(memoSingleGameService.getSinglePlayerByGameIdFromList(any(UUID.class))).thenReturn(singlePlayer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/leaveGame/{sessionId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void isPlayValidTestThenValid() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(memoSingleGameService.getSinglePlayerByGameIdFromList(any(UUID.class))).thenReturn(singlePlayer);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/isPlayValid/{sessionId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.guessedBoard").exists())
                .andExpect(jsonPath("$.remainingTime").exists())
                .andExpect(jsonPath("$.numOfGuessed").exists())
                .andExpect(jsonPath("$.cards").exists());
    }

    @Test
    public void isPlayValidTestThenInvalid() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(memoSingleGameService.getSinglePlayerByGameIdFromList(any(UUID.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/singlePlayer/isPlayValid/{sessionId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(false));
    }
}
