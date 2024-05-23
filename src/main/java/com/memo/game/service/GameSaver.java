package com.memo.game.service;

import java.util.UUID;

/**
 * Interface for saving game details after a game session has ended.
 */
public interface GameSaver {
    /**
     * Saves the game details after the game has ended.
     *
     * @param gameId the unique identifier of the game session
     * @param won indicates whether the game was won or lost
     * @param timeRemaining the amount of time remaining when the game ended, zero if the game was lost
     * @param pairs the total number of pairs involved in the game, relevant for memory matching games
     * @param initialTime the initial amount of time allocated for the game, useful for performance evaluation
     */
    void saveGameAfterEnded(UUID gameId, boolean won, int timeRemaining,
                  int pairs, int initialTime);
}
