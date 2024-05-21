package com.memo.game.service;

import com.memo.game.entity.MemoUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class TokenServiceTest {

    private static TokenService tokenService;
    @Mock
    private static TokenBlacklistService tokenBlacklistService;

    private static final MemoUser MEMO_USER = new MemoUser(
            "name01",
            "name01@gmail.com",
            "password"
    );

    @BeforeAll
    public static void setUp() {
        TokenBlacklistService tokenBlacklistService = mock(TokenBlacklistService.class);
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);
        MEMO_USER.setId(UUID.randomUUID());
        tokenService = new TokenService(tokenBlacklistService);
    }

    @Test
    public void isTokeValidTest() {
        String token = tokenService.generateJwtToken(MEMO_USER);
        assertThat(tokenService.isTokenValid(token)).isTrue();
    }

    @Test
    public void isUserFoundTest() {
        String token = tokenService.generateJwtToken(MEMO_USER);
        assertThat(tokenService.extractUserIdFromToken(token)).isEqualTo(MEMO_USER.getId());
    }
}
