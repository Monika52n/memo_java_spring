package com.memo.game.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class TokenBlackListServiceTest {
    private final TokenBlacklistService tokenBlacklistService = new TokenBlacklistService();

    @Test
    public void addTokenToBlackList() {
        tokenBlacklistService.addToBlacklist("token");
        assertThat(tokenBlacklistService.isBlacklisted("token")).isTrue();
    }

    @Test
    public void tokenNotAddedToList() {
        tokenBlacklistService.addToBlacklist("token");
        assertThat(tokenBlacklistService.isBlacklisted("tok")).isFalse();
    }
}
