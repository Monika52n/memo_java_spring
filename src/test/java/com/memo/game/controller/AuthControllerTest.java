package com.memo.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memo.game.dto.AuthRequest;
import com.memo.game.entity.MemoUser;
import com.memo.game.service.UserService;
import com.memo.game.service.TokenBlacklistService;
import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    private AuthRequest authRequest;
    private MemoUser memoUser;
    private String token;
    String userName = "alma";
    String email = "alma@gmail.com";
    String password = "alma12";
    @BeforeEach
    public void setUp() {
        UUID userId = UUID.randomUUID();
        authRequest = new AuthRequest();
        authRequest.setUsername(userName);
        authRequest.setEmail(email);
        authRequest.setPassword(password);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        memoUser = new MemoUser(userName, email, hashedPassword);
        memoUser.setId(userId);
        token = "token123";
        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        when(tokenBlacklistService.addToBlacklist(any())).thenReturn(false);
    }

    private AuthRequest createAuthRequest(String userName, String password, String email) {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail(email);
        authRequest.setUsername(userName);
        authRequest.setPassword(password);
        return authRequest;
    }

    @Test
    public void registerTest() throws Exception {
        when(userService.getByUserName(any())).thenReturn(null);
        when(userService.getByEmail(any())).thenReturn(null);
        when(userService.saveUser(any())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void whenRegisterRequestDataIsIncompleteThenBadRequestReturned() throws Exception {
        authRequest.setUsername(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect request data!"));
    }

    @Test
    public void whenExistingEmailRegisterThenBadRequest() throws Exception {
        when(userService.getByUserName(any())).thenReturn(null);
        when(userService.getByEmail(any())).thenReturn(memoUser);
        when(userService.saveUser(any())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Existing email!")));
    }

    @Test
    public void whenExistingUserNameRegisterThenBadRequest() throws Exception {
        when(userService.getByUserName(any())).thenReturn(memoUser);
        when(userService.getByEmail(any())).thenReturn(null);
        when(userService.saveUser(any())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Existing username!")));
    }

    @Test
    public void whenIncorrectEmailFormatRegisterThenBadRequest() throws Exception {
        when(userService.getByUserName(any())).thenReturn(null);
        when(userService.getByEmail(any())).thenReturn(null);
        when(userService.saveUser(any())).thenReturn(null);
        authRequest.setEmail("almavhjfehabr");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Incorrect email format!")));
    }

    @Test
    public void whenIncorrectUserNameRegisterThenBadRequest() throws Exception {
        when(userService.getByUserName(any())).thenReturn(null);
        when(userService.getByEmail(any())).thenReturn(null);
        when(userService.saveUser(any())).thenReturn(null);
        authRequest.setUsername("al");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username must be at least 4 characters long with no whitespaces!")));
    }

    @Test
    public void whenIncorrectPasswordRegisterThenBadRequest() throws Exception {
        when(userService.getByUserName(any())).thenReturn(null);
        when(userService.getByEmail(any())).thenReturn(null);
        when(userService.saveUser(any())).thenReturn(null);
        authRequest.setPassword("   dcfvunk ");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password must be at least 6 characters long with no whitespaces!")));
    }

    @Test
    public void signInTest() throws Exception {
        when(userService.getByUserName(memoUser.getUserName())).thenReturn(memoUser);
        when(userService.getByEmail(memoUser.getEmail())).thenReturn(memoUser);
        when(tokenService.generateJwtToken(memoUser)).thenReturn(token);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signIn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(token)));
    }

    @Test
    public void whenSignInRequestDataIsIncompleteThenBadRequestReturned() throws Exception {
        authRequest.setUsername(null);
        authRequest.setEmail(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect request data!"));
    }

    @Test
    public void whenEmailOrUserNameIsIncorrectSignInThenUnauthorized() throws Exception {
        when(userService.getByUserName(memoUser.getUserName())).thenReturn(null);
        when(userService.getByEmail(memoUser.getEmail())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Email or username is incorrect!")));
    }

    @Test
    public void whenPasswordIsIncorrectSignInThenUnauthorized() throws Exception {
        when(userService.getByUserName(memoUser.getUserName())).thenReturn(memoUser);
        when(userService.getByEmail(memoUser.getEmail())).thenReturn(memoUser);
        authRequest.setPassword("incorrectPassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Password is incorrect!")));
    }

    @Test
    public void whenTokenIsInvalidGetUserInfoThenBadRequest() throws Exception {
        String invalidToken = "invalid-token";
        when(tokenService.extractTokenFromRequest(any())).thenReturn(invalidToken);
        when(tokenService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/getUserInfo")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void whenUserNotFoundGetUserInfoShouldReturnNotFound() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.extractUserIdFromToken(token)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/getUserInfo")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found!")));
    }

    @Test
    public void whenTokenIsValidGetUserInfoShouldReturnUserInfo() throws Exception {
        when(tokenService.extractTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(tokenService.isTokenValid(token)).thenReturn(true);
        when(tokenService.extractUserIdFromToken(token)).thenReturn(memoUser.getId());
        when(userService.getUserNameById(memoUser.getId())).thenReturn(memoUser.getUserName());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/getUserInfo")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(memoUser.getId().toString()))
                .andExpect(jsonPath("$.userName").value(memoUser.getUserName()));
    }
}
