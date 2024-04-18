package com.memo.game.controller;

import com.memo.game.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public class MemoRestController {
    private final TokenService tokenService;

    @Autowired
    public MemoRestController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    protected Object getTokenError(HttpServletRequest request) {
        String token = tokenService.extractTokenFromRequest(request);
        if (!tokenService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return null;
    }

    protected Object getUserError(String token) {
        UUID userId = tokenService.extractUserIdFromToken(token);
        if(userId==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }
        return null;
    }

    protected String getToken(HttpServletRequest request) {
        return tokenService.extractTokenFromRequest(request);
    }

    protected UUID getUserFromToken(String token) {
        return tokenService.extractUserIdFromToken(token);
    }
}
