package com.memo.game.model;

import java.util.UUID;

public interface GameSaver {
    void saveGameAfterEnded(UUID gameId, boolean won, int timeRemaining,
                  int pairs, int initialTime);
}
