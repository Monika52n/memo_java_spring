package com.memo.game.model;

import java.util.*;

public abstract class MemoGame {
    protected UUID playId;
    protected int[] board;
    protected Boolean[] isGuessedBoard;
    protected final Timer timer = new Timer();
    protected int firstCardIndex = -1;
    protected boolean isGameOver = false;
    protected boolean arePreviousCardsequal = false;

    protected void configGame(int numberOfPairs) {
        if(numberOfPairs<=0) {
            throw new IllegalArgumentException("Number of pairs must be positive.");
        }
        board = new int[numberOfPairs*2];
        isGuessedBoard = new Boolean[numberOfPairs*2];
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
    }

    public UUID getPlayId() {
        return playId;
    }

    public boolean getIsGameOver() {return isGameOver;}

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

    protected Map<Integer, Integer> getOneCard(int index) {
        if(index<0 || index>= board.length) {
            throw new ArrayIndexOutOfBoundsException("Index " + index + " is out of bounds for array length " + board.length);
        }
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        if(!isGameOver) {
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

    protected abstract void gameEnded();
}
