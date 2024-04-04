package com.memo.game.model;

import java.util.*;

public class MultiPlayer extends MemoGame {
    private UUID player1Id;
    private UUID player2Id;
    private boolean isPlayer1sTurn = true;
    private int player1GuessedCards = 0;
    private int player2GuessedCards = 0;
    private String winner = "draw";
    private boolean isGameStarted = false;

    public MultiPlayer(int numberOfPairs, UUID player1Id, UUID player2Id) {
        board = new int[numberOfPairs*2];
        this.player1Id = player1Id;
        this.player2Id = player2Id;

        configGame(numberOfPairs);
    }

    public Map<Integer, Integer> getCard(UUID player, int index) {
        if(player1Id==null || player2Id==null) {
            throw new IllegalArgumentException("There are missing players!");
        }
        if(!((isPlayer1sTurn && player.equals(player1Id))
                || (!isPlayer1sTurn && (player.equals(player2Id))))) {
            return new HashMap<>();
        }

        isGameStarted = true;
        Map<Integer, Integer> map = getOneCard(index);

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

    @Override
    protected void gameEnded() {
        isGameOver=true;
        for (boolean guessedCards : isGuessedBoard) {
            if (!guessedCards) {
                isGameOver = false;
                break;
            }
        }
        if(player1GuessedCards >= isGuessedBoard.length/2+1 ||
                player2GuessedCards >= isGuessedBoard.length/2+1) {
            isGameOver = true;
        }
        if(isGameOver) {
            if(player1GuessedCards>player2GuessedCards) {
                winner = player1Id.toString();
            } else if(player1GuessedCards<player2GuessedCards) {
                winner = player2Id.toString();
            }
        }
    }

    public String getWinner() {
        return winner;
    }

    public int getPlayer1GuessedCards() {
        return player1GuessedCards;
    }

    public int getPlayer2GuessedCards() {
        return  player2GuessedCards;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer1Id(UUID player1Id) {
        this.player1Id = player1Id;
    }

    public void setPlayer2Id(UUID player2Id) {
        this.player2Id = player2Id;
    }

    public int getNumberOfPairs() {
       return board.length;
    }

    public void playerLeaves(UUID player) {
        if(isGameStarted) {
            if(player.equals(player1Id)) {
                winner=player2Id.toString();
                isGameOver=true;
            }
            if(player.equals(player2Id)) {
                winner=player1Id.toString();
                isGameOver=true;
            }
        } else {
            if(player.equals(player1Id) && player2Id==null) {
                isGameOver = true;
            }
            if(player1Id!=null && player.equals(player2Id)) {
                player2Id = null;
            }
            if(player2Id!=null && player.equals(player1Id)) {
                player1Id = player2Id;
                player2Id = null;
            }
        }
    }
}
