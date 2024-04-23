package com.memo.game.service;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.memo.game.entity.MemoUsers;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        return token != null && !tokenBlacklistService.isBlacklisted(token);
    }

    public String generateJwtToken(MemoUsers user) {
        long expirationTime = 3600000;
        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expirationTime);

        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256("ckml43wekmv44lINJXN66e54clk2N34KJLKMS"));
    }

    public UUID extractUserIdFromToken(String token) {
        try {
            if (token != null) {
                DecodedJWT decodedJWT = JWT.decode(token);
                return UUID.fromString(decodedJWT.getSubject());
            }
        } catch (JWTDecodeException e) {
            return null;
        }
        return null;
    }
}