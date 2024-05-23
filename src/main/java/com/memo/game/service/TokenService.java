package com.memo.game.service;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.memo.game.entity.MemoUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Service class for managing tokens.
 */
@Service
public class TokenService {
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    public TokenService() {}
    public TokenService(TokenBlacklistService tokenBlacklistService) {this.tokenBlacklistService=tokenBlacklistService;}

    public void setTokenBlacklistService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Extracts the JWT token from the Authorization header of an HTTP request.
     *
     * This method retrieves the JWT token from the Authorization header of an HTTP request.
     * If the header is present and starts with "Bearer ", the method extracts and returns the token.
     * If the header is not present or does not start with "Bearer ", it returns null.
     *
     * @param request the HTTP request from which to extract the token
     * @return the JWT token extracted from the request, or null if not found
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Checks if a JWT token is valid and not blacklisted.
     *
     * This method verifies if a JWT token is not null and not present in the token blacklist.
     * If the token is null or blacklisted, it returns false; otherwise, it returns true.
     *
     * @param token the JWT token to be validated
     * @return true if the token is valid and not blacklisted, false otherwise
     */
    public boolean isTokenValid(String token) {
        return token != null && !tokenBlacklistService.isBlacklisted(token);
    }

    /**
     * Generates a JWT token for a given user.
     *
     * This method generates a JWT token for a specified user with a predefined expiration time.
     * It includes the user's ID as the token subject and signs the token with a secret key.
     * The generated token is returned as a string.
     *
     * @param user the user for whom the token is generated
     * @return the generated JWT token
     */
    public String generateJwtToken(MemoUser user) {
        long expirationTime = 3600000;
        Date issuedAt = new Date();
        Date expirationDate = new Date(issuedAt.getTime() + expirationTime);

        return JWT.create()
                .withSubject(user.getId().toString())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256("ckml43wekmv44lINJXN66e54clk2N34KJLKMS"));
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * This method parses a JWT token and extracts the user ID from its subject claim.
     * If the token is valid and contains a subject claim, the method returns the extracted user ID as a UUID.
     * If the token is invalid or does not contain a subject claim, it returns null.
     *
     * @param token the JWT token from which to extract the user ID
     * @return the user ID extracted from the token, or null if extraction fails or the token is invalid
     */
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