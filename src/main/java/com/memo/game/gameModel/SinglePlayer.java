package com.memo.game.gameModel;

import com.memo.game.service.GameSaver;

import java.util.*;

/**
 * Represents a single-player game session extending the MemoGame class.
 * Manages the gameplay, including flipping cards, running a timer,
 * tracking guessed pairs, determining the result, and managing game state.
 * The player is flipping cards until winning if all pairs are guessed, leaves,
 * or runs out of time.
 * Inherits game initialization and end condition verification from the MemoGame superclass.
 */
public class SinglePlayer extends MemoGame {

    /** Flag indicating whether the game timer is running. */
    private boolean isTimerRunning;

    /** The initial time allocated for the game. */
    private final int initialTime;

    /** The remaining time for the game. */
    private final Timer timer = new Timer();

    /** The remaining time for the game. */
    private int timeRemaining;

    /** The service responsible for saving game data. */
    private final GameSaver gameSaver;

    /** Flag indicating whether the player won the game. */
    private boolean won = false;

    /**
     * Constructs a SinglePlayer game instance with the specified parameters.
     *
     * @param numberOfPairs The number of pairs in the game.
     * @param initialTime The initial time allocated for the game.
     * @param gameSaver The service responsible for saving game data.
     * @throws IllegalArgumentException if the initial time is not positive.
     */
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

    /**
     * Starts the game timer.
     */
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

    /**
     * Stops the game timer.
     */
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

    /**
     * Flips a card in the game.
     *
     * @param index The index of the card to flip.
     * @return A map containing the index and value of the flipped card, if it can be flipped
     */
    public Map<Integer, Integer> flipCard(int index) {
        return flipOneCard(index);
    }

    /**
     * This method checks whether the game has ended. If there's remaining time,
     * it verifies whether all pairs have been guessed. If so, it marks the game
     * as over and determines if the player won. If time runs out, the game is
     * ended automatically. This method is invoked internally to handle the game
     * state transitions.
     */
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

    /**
     * Leaves the game, ending it prematurely.
     */
    public void leaveGame() {
        isGameOver = true;
        stopTimer();
        timeRemaining=0;
        saveGame();
    }

    /**
     * Saves the game state after it ends.
     */
    private void saveGame() {
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
    public int getTimeRemaining() {return timeRemaining;}
    public boolean getWon() {return won;}
    public int getInitialTime() {return  initialTime;}
}