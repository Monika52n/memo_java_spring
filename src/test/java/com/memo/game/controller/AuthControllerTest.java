package com.memo.game.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memo.game.dto.AuthRequest;
import com.memo.game.entity.MemoUsers;
import com.memo.game.service.MemoUsersService;
import com.memo.game.service.TokenBlacklistService;
import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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
    private MemoUsersService memoUsersService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
    private AuthRequest authRequest;
    private MemoUsers memoUser;
    private String token;
    @BeforeEach
    public void setUp() {
        String userName = "alma";
        String email = "alma@gmail.com";
        String password = "alma12";
        UUID userId = UUID.randomUUID();
        authRequest = new AuthRequest();
        authRequest.setUsername(userName);
        authRequest.setEmail(email);
        authRequest.setPassword(password);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        memoUser = new MemoUsers(userName, email, hashedPassword);
        memoUser.setId(userId);
        token = "token123";
        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        when(tokenBlacklistService.addToBlacklist(any())).thenReturn(false);
    }

    @Test
    public void registerTest() throws Exception {
        when(memoUsersService.getByUserName(any())).thenReturn(null);
        when(memoUsersService.getByEmail(any())).thenReturn(null);
        when(memoUsersService.saveUser(any())).thenReturn(null);

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
        when(memoUsersService.getByUserName(any())).thenReturn(null);
        when(memoUsersService.getByEmail(any())).thenReturn(memoUser);
        when(memoUsersService.saveUser(any())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Existing email!")));
    }

    @Test
    public void whenExistingUserNameRegisterThenBadRequest() throws Exception {
        when(memoUsersService.getByUserName(any())).thenReturn(memoUser);
        when(memoUsersService.getByEmail(any())).thenReturn(null);
        when(memoUsersService.saveUser(any())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Existing username!")));
    }

    @Test
    public void whenIncorrectEmailFormatRegisterThenBadRequest() throws Exception {
        when(memoUsersService.getByUserName(any())).thenReturn(null);
        when(memoUsersService.getByEmail(any())).thenReturn(null);
        when(memoUsersService.saveUser(any())).thenReturn(null);
        authRequest.setEmail("incorrectGmail");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Incorrect email format!")));
    }

    @Test
    public void signInTest() throws Exception {
        when(memoUsersService.getByUserName(memoUser.getUserName())).thenReturn(memoUser);
        when(memoUsersService.getByEmail(memoUser.getEmail())).thenReturn(memoUser);
        when(memoUsersService.signIn(memoUser.getId())).thenReturn(true);
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
    public void whenAlreadySignedInThenBadRequest() throws Exception {
        memoUser.setSignedIn(true);
        when(memoUsersService.getByUserName(memoUser.getUserName())).thenReturn(memoUser);
        when(memoUsersService.getByEmail(memoUser.getEmail())).thenReturn(memoUser);
        when(memoUsersService.signIn(memoUser.getId())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signIn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("User already signed in.")));
    }

    @Test
    public void whenEmailOrUserNameIsIncorrectSignInThenUnauthorized() throws Exception {
        when(memoUsersService.getByUserName(memoUser.getUserName())).thenReturn(null);
        when(memoUsersService.getByEmail(memoUser.getEmail())).thenReturn(null);
        when(memoUsersService.signIn(memoUser.getId())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Email or username is incorrect!")));
    }

    @Test
    public void whenPasswordIsIncorrectSignInThenUnauthorized() throws Exception {
        when(memoUsersService.getByUserName(memoUser.getUserName())).thenReturn(memoUser);
        when(memoUsersService.getByEmail(memoUser.getEmail())).thenReturn(memoUser);
        when(memoUsersService.signIn(memoUser.getId())).thenReturn(true);
        authRequest.setPassword("incorrectPassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Password is incorrect!")));
    }

    @Test
    public void whenTokenIsValidSignOutThenSignOutAndNoContent() throws Exception {
        String validToken = "valid-token";
        when(tokenService.extractTokenFromRequest(any())).thenReturn(validToken);
        when(tokenService.isTokenValid(validToken)).thenReturn(true);
        when(tokenService.extractUserIdFromToken(validToken)).thenReturn(memoUser.getId());
        when(memoUsersService.signOut(memoUser.getId())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signOut")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        verify(memoUsersService).signOut(memoUser.getId());
        verify(tokenBlacklistService).addToBlacklist(validToken);
    }

    @Test
    public void whenTokenIsInvalidSignOutThenBadRequest() throws Exception {
        String invalidToken = "invalid-token";
        when(tokenService.extractTokenFromRequest(any())).thenReturn(invalidToken);
        when(tokenService.isTokenValid(invalidToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/signOut")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isBadRequest());

        verify(memoUsersService, never()).signOut(any());
        verify(tokenBlacklistService, never()).addToBlacklist(anyString());
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
        when(memoUsersService.getUserNameById(memoUser.getId())).thenReturn(memoUser.getUserName());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/getUserInfo")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(memoUser.getId().toString()))
                .andExpect(jsonPath("$.userName").value(memoUser.getUserName()));
    }
}
