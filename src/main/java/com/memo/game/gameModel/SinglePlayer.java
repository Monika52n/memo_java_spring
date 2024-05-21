package com.memo.game.gameModel;

import com.memo.game.service.GameSaver;

import java.util.*;

public class SinglePlayer extends MemoGame {
    private boolean isTimerRunning;
    private final int initialTime;
    private final Timer timer = new Timer();
    private int timeRemaining;
    private final GameSaver gameSaver;
    protected boolean won = false;

    public SinglePlayer(int numberOfPairs, int initialTime, GameSaver gameSaver) {
        if(initialTime<=0) {
            throw new IllegalArgumentException("Initial time must be positive.");
        }
        this.gameSaver=gameSaver;
        this.timeRemaining = initialTime;
        this.initialTime = initialTime;
        this.isTimerRunning = false;
        configGame(numberOfPairs);
        startTimer();
    }

    public int getTimeRemaining() {return timeRemaining;}
    public boolean getWon() {return won;}
    public int getInitialTime() {return  initialTime;}

    private void startTimer() {
        if (!isTimerRunning) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (timeRemaining > 0) {
                        timeRemaining--;
                    } else {
                        isGameOver=true;
                        stopTimer();
                        saveGame();
                    }
                }
            }, 1000, 1000);
            isTimerRunning = true;
        }
    }

    private void stopTimer() {
        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = false;
        }
    }

    public int getNumOfGuessedPairs() {
        int sum = 0;
        for (Boolean pair : isGuessedBoard) {
            if (pair) {
                sum++;
            }
        }
        return sum/2;
    }

    public Map<Integer, Integer> getCard(int index) {
        return getOneCard(index);
    }

    @Override
    protected void gameEnded() {
        if(timeRemaining>0) {
            isGameOver=true;
            for (boolean guessedCards : isGuessedBoard) {
                if (!guessedCards) {
                    isGameOver = false;
                    break;
                }
            }
            if(isGameOver) {
                won = true;
                stopTimer();
                saveGame();
            }
        } else {
            isGameOver = true;
        }
    }

    public void leaveGame() {
        isGameOver = true;
        stopTimer();
        timeRemaining=0;
        saveGame();
    }

    public void saveGame() {
        gameSaver.saveGameAfterEnded(playId, won, timeRemaining, board.length/2, initialTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SinglePlayer that = (SinglePlayer) o;
        return Objects.equals(playId, that.getPlayId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(playId);
    }
}