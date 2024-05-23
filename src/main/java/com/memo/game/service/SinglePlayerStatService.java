package com.memo.game.service;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.dto.ModeStat;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing and retrieving statistics related to single-player games.
 */
@Service
public class SinglePlayerStatService {
    private final ArrayList<ModeStat> modeStats = new ArrayList<ModeStat>();

    public SinglePlayerStatService() {}

    /**
     * Adds a list of single-player game records to calculate mode statistics.
     *
     * This method takes a list of single-player game records and calculates mode statistics based on the games played.
     * It clears the existing mode statistics list, then iterates through each game record to update the statistics.
     * After processing all game records, it returns the updated mode statistics list.
     *
     * @param games the list of single-player game records
     * @return the updated list of mode statistics
     */
    public List<ModeStat> addList(List<MemoSingleGame> games) {
        modeStats.clear();
        for(MemoSingleGame game : games) {
            addGame(game.getPairs(), game.getTimeMax(), game.isWon(), game.getRemainingTime());
        }
        return modeStats;
    }

    /**
     * Adds a single game record to update mode statistics.
     *
     * This method adds a single game record to the mode statistics.
     * It checks if a similar game (with the same pairs and time) already exists in the mode statistics.
     * If found, it updates the statistics accordingly (wins, losses, and remaining time).
     * If not found, it adds a new entry to the mode statistics.
     *
     * @param pairs the number of pairs in the game
     * @param time the maximum time allowed for the game
     * @param won true if the game was won, false otherwise
     * @param remainingTime the remaining time in the game (if won)
     */
    private void addGame(int pairs, int time, boolean won, int remainingTime) {
        boolean found = false;
        for(ModeStat modeStatToFind : modeStats) {
            if(modeStatToFind.getPairs()==pairs &&
                    modeStatToFind.getTime()==time) {
                found = true;
                if(won) {
                    modeStatToFind.incrementWins(1);
                    modeStatToFind.incrementSumRemainingTime(remainingTime);
                } else {
                    modeStatToFind.incrementLosses(1);
                }
            }
        }
        if(!found) {
            if(won) {
                modeStats.add(new ModeStat(time, pairs, 1, 0, remainingTime));
            } else {
                modeStats.add(new ModeStat(time, pairs, 0, 1, remainingTime));
            }
        }
    }
}

