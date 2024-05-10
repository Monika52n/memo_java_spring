package com.memo.game.service;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.entity.ModeStat;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SinglePlayerCreateStatService  {
    private final ArrayList<ModeStat> modeStats = new ArrayList<ModeStat>();

    public SinglePlayerCreateStatService() {}

    public List<ModeStat> addList(List<MemoSingleGame> games) {
        modeStats.clear();
        for(MemoSingleGame game : games) {
            addGame(game.getPairs(), game.getTimeMax(), game.isWon(), game.getRemainingTime());
        }
        return modeStats;
    }

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

