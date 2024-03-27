package com.memo.game.model;

import java.util.*;

public class SinglePlayer {
    private UUID playId;
    private boolean isTimerRunning;
    private final int[] board;
    private final boolean[] isGuessedBoard;
    private final int initialTime;
    private final Timer timer = new Timer();
    private int timeRemaining;
    private int firstCardIndex = -1;
    private boolean won = false;
    private boolean isGameOver = false;
    private final GameSaver gameSaver;
    private boolean arePreviousCardsequal = false;

    public SinglePlayer(int numberOfPairs, int initialTime, GameSaver gameSaver) {
        if(numberOfPairs<=0 || initialTime<=0) {
            throw new IllegalArgumentException("Number of pairs and initial time must be positive.");
        }
        this.gameSaver=gameSaver;
        this.timeRemaining = initialTime;
        this.initialTime = initialTime;
        this.isTimerRunning = false;
        board = new int[numberOfPairs*2];
        isGuessedBoard = new boolean[numberOfPairs*2];
        int index = 0;
        for(int i=1; i<=numberOfPairs; i++) {
            isGuessedBoard[index]=false;
            board[index++] = i;
            isGuessedBoard[index]=false;
            board[index++] = i;
        }

        // Shuffle the array using Fisher-Yates algorithm
        Random random = new Random();
        for (int i = board.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = board[i];
            board[i] = board[j];
            board[j] = temp;
        }

        playId = UUID.randomUUID();
        startTimer();
    }

    public UUID getPlayId() {
        return playId;
    }

    public boolean getIsGameOver() {return isGameOver;}

    public boolean getWon() {return won;}

    public int getTimeRemaining() {return timeRemaining;}

    public Integer[] getGuessedBoard() {
        Integer[] guessedBoard = new Integer[board.length];
        for(int i=0; i< board.length; i++) {
            if(isGuessedBoard[i]) {
                guessedBoard[i]=board[i];
            }
        }
        return guessedBoard;
    }

    public boolean getArePreviousCardsequal() {
        return arePreviousCardsequal;
    }

    public Map<Integer, Integer> getCard(int index) {
        if(index<0 || index>= board.length) {
            throw new ArrayIndexOutOfBoundsException("Index " + index + " is out of bounds for array length " + board.length);
        }
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        if(timeRemaining>0) {
            if (firstCardIndex == -1) {
                map.put(index, getFirstCard(index));
            } else {
                if(firstCardIndex==index) {
                    throw new IllegalArgumentException("The index is the same as previous.");
                }
                Tuple<Integer, Integer> values = getSecondAndFirstCard(index);
                map.put(firstCardIndex, values.getFirst());
                map.put(index, values.getSecond());
                firstCardIndex = -1;
            }
            gameEnded();
        }
        return(map);
    }

    private int getFirstCard(int index1) {
        this.firstCardIndex = index1;
        arePreviousCardsequal = false;
        return board[index1];
    }

    private Tuple<Integer, Integer> getSecondAndFirstCard(int index2) {
        if(board[firstCardIndex]==board[index2]) {
            isGuessedBoard[firstCardIndex] = true;
            isGuessedBoard[index2] = true;
            arePreviousCardsequal=true;
        } else {
            arePreviousCardsequal=false;
        }
        return (new Tuple<Integer, Integer>(board[firstCardIndex], board[index2]));
    }

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

    private void gameEnded() {
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
        }
    }

    private void saveGame() {
        gameSaver.saveGameAfterEnded(playId, won, timeRemaining, board.length/2, initialTime);
    }
}