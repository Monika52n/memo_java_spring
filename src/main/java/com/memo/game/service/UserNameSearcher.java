package com.memo.game.service;

import java.util.UUID;

/**
 * Interface for retrieving usernames based on their unique identifiers.
 */
public interface UserNameSearcher {
    /**
     * Retrieves the username for a given user ID.
     *
     * @param id the unique identifier (UUID) of the user
     * @return the username associated with the given ID, or null if no user found
     */
    public String getUserNameById(UUID id);
}
