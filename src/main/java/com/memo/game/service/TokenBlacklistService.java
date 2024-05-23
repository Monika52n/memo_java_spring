package com.memo.game.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Service for managing blacklisted tokens.
 */
@Service
public class TokenBlacklistService {

    private final Set<String> blacklist = new HashSet<>();

    /**
     * Adds a token to the blacklist.
     *
     * This method adds a token to the blacklist, indicating that it should not be considered valid for authentication.
     *
     * @param token the token to be added to the blacklist
     * @return true if the token is successfully added to the blacklist, false otherwise
     */
    public boolean addToBlacklist(String token) {
        return blacklist.add(token);
    }

    /**
     * Checks if a token is blacklisted.
     *
     * This method checks if a token is present in the blacklist, indicating that it should not be considered valid for authentication.
     *
     * @param token the token to be checked
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
