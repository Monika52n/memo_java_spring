package com.memo.game.model;

import java.util.*;

public class MultiPlayer {
    private UUID playId;
    private final int[] board;
    private final boolean[] isGuessedBoard;
    private int firstCardIndex = -1;
    private boolean won = false;
    private boolean isGameOver = false;
    private boolean arePreviousCardsequal = false;
    UUID player1Id;
    UUID player2Id;
    boolean isPlayer1sTurn = true;

    int player1GuessedCards = 0;
    int player2GuessedCards = 0;

    public MultiPlayer(int numberOfPairs, UUID player1Id, UUID player2Id) {
        board = new int[numberOfPairs*2];
        this.player1Id = player1Id;
        this.player2Id = player2Id;

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
    }

    public Map<Integer, Integer> getCard(UUID player, int index) {
        if(!((isPlayer1sTurn && player.equals(player1Id))
                || (!isPlayer1sTurn && (player.equals(player2Id))))) {
            return new HashMap<>();
        }

        if(index<0 || index>= board.length) {
            throw new ArrayIndexOutOfBoundsException("Index " + index + " is out of bounds for array length " + board.length);
        }
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
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

        if(arePreviousCardsequal) {
            if(isPlayer1sTurn) {
                player1GuessedCards++;
            } else {
                player2GuessedCards++;
            }
        }
        isPlayer1sTurn = !isPlayer1sTurn;
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
}
