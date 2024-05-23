package com.memo.game.gameModel;

import java.util.*;

/**
 * The MemoGame class represents the abstract model for a memory game.
 * It provides methods for flipping cards, managing game state, and determining game completion.
 */
public abstract class MemoGame {
    /**
     * The unique identifier for the game session.
     */
    protected UUID playId;
    /**
     * The array representing the game board containing card values.
     */
    protected int[] board;
    /**
     * An array indicating whether each card on the board has been guessed.
     */
    protected Boolean[] isGuessedBoard;
    /**
     * The index of the first card flipped in a turn. Initialized to -1.
     */
    protected int firstCardIndex = -1;
    /**
     * A boolean indicating whether the game is over.
     */
    protected boolean isGameOver = false;
    /**
     * A boolean indicating whether the previous two flipped cards are equal.
     */
    protected boolean arePreviousCardsEqual = false;

    /**
     * Configures the game by generating the game board and initializing game settings.
     *
     * @param numberOfPairs The number of pairs to be included in the game.
     * @throws IllegalArgumentException if the number of pairs is not positive.
     */
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

    /**
     * Flips the card state at a given index.
     * Determines whether it's the first or second card flipped and calls corresponding methods.
     *
     * @param index The index of the card to be flipped.
     * @return A map containing the index and value of the flipped card.
     */
    protected Map<Integer, Integer> flipOneCard(int index) {
        if(index<0 || index>= board.length) {
            throw new ArrayIndexOutOfBoundsException("Index " + index + " is out of bounds for array length " + board.length);
        }
        if(index==firstCardIndex) {
            throw new IllegalArgumentException("Index (" +  index + ") is same as the previous (" + firstCardIndex + ").");
        }
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        if(!isGameOver) {
            if (firstCardIndex == -1) {
                map.put(index, flipFirstCard(index));
            } else {
                AbstractMap.SimpleEntry<Integer, Integer> values = flipSecondAndFirstCard(index);
                map.put(firstCardIndex, values.getKey());
                map.put(index, values.getValue());
                firstCardIndex = -1;
            }
            gameEnded();
        }
        return(map);
    }

    /**
     * Flips the first card and sets the index of the first card flipped.
     *
     * @param index1 The index of the first card flipped.
     * @return The value of the first card flipped.
     */
    private int flipFirstCard(int index1) {
        this.firstCardIndex = index1;
        arePreviousCardsEqual = false;
        return board[index1];
    }

    /**
     * Flips the second and first cards, checks if they match, and updates the game state accordingly.
     *
     * @param index2 The index of the second card flipped.
     * @return An AbstractMap.SimpleEntry containing the values of the flipped cards.
     */
    private AbstractMap.SimpleEntry<Integer, Integer> flipSecondAndFirstCard(int index2) {
        if(board[firstCardIndex]==board[index2]) {
            isGuessedBoard[firstCardIndex] = true;
            isGuessedBoard[index2] = true;
            arePreviousCardsEqual =true;
        } else {
            arePreviousCardsEqual =false;
        }
        return (new AbstractMap.SimpleEntry(board[firstCardIndex], board[index2]));
    }

    /**
     * Method to be implemented by subclasses to handle game ending logic.
     */
    /**
     * It determines whether the game has ended and, if so, performs any necessary
     * actions based on the game mode (e.g., determining the winner, stopping timers, and saving
     * the game state).
     */
    protected abstract void gameEnded();

    public UUID getPlayId() {
        return playId;
    }

    public boolean isGameOver() {return isGameOver;}

    public Integer[] getGuessedBoard() {
        Integer[] guessedBoard = new Integer[board.length];
        for(int i=0; i< board.length; i++) {
            if(isGuessedBoard[i]) {
                guessedBoard[i]=board[i];
            }
        }
        return guessedBoard;
    }

    public Map<Integer, Integer> getPreviousMove() {
        Map<Integer, Integer> cards = new HashMap<>();
        if(firstCardIndex!=-1) {
            cards.put(firstCardIndex, board[firstCardIndex]);
        }
        return cards;
    }

    public boolean getArePreviousCardsEqual() {
        return arePreviousCardsEqual;
    }

    public int getNumberOfPairs() {
        return board.length/2;
    }

    public void setBoard(int[] board) {
        if(board.length==this.board.length) {
            this.board = board;
        }
    }

    public void setGameOver(boolean isGameOver) {
        this.isGameOver=isGameOver;
    }
}
