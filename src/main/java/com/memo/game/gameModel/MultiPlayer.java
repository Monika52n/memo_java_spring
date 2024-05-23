package com.memo.game.gameModel;

import java.util.*;

/**
 * Represents a multiplayer game session extending the MemoGame class.
 * Manages the gameplay between two players, including flipping cards,
 * tracking guessed pairs, determining the winner, and managing game state.
 * Each player takes turns flipping cards until one of them wins, all pairs are guessed,
 * or one player leaves.
 * Inherits game initialization and end condition verification from the MemoGame superclass.
 */
public class MultiPlayer extends MemoGame {
    /** Unique identifiers for the two players */
    private UUID player1Id;
    private UUID player2Id;

    /** Indicates whose turn it is to play */
    private boolean isPlayer1sTurn = true;

    /** Count of guessed cards for each player*/
    private int player1GuessedCards = 0;
    private int player2GuessedCards = 0;

    /** Stores the winner's UUID or "draw" if game ends in a tie */
    private String winner = "draw";

    /** Flag indicating if the game has started */
    private boolean isGameStarted = false;

    /**
     * Constructs a MultiPlayer game session with the specified number of pairs and player IDs.
     *
     * @param numberOfPairs the number of pairs in the game
     * @param player1Id     the UUID of the first player
     * @param player2Id     the UUID of the second player
     */
    public MultiPlayer(int numberOfPairs, UUID player1Id, UUID player2Id) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;

        configGame(numberOfPairs);
    }

    /**
     * Flips a card at the specified index for the given player.
     * Only allows valid moves based on game state and player turns.
     * If the move is valid, updates the game state and returns the flipped card.
     *
     * @param player the UUID of the player making the move
     * @param index  the index of the card to flip
     * @return a map containing the index and value of the flipped card
     */
    public Map<Integer, Integer> flipCard(UUID player, int index) {
        if(player1Id==null || player2Id==null) {
            throw new IllegalArgumentException("There are missing players!");
        }
        if(!((isPlayer1sTurn && player.equals(player1Id))
                || (!isPlayer1sTurn && (player.equals(player2Id))))
                || !isGameStarted
                || (getGuessedBoard())[index]!=null) {
            return new HashMap<>();
        }

        Map<Integer, Integer> map = flipOneCard(index);

        if(firstCardIndex==-1) {
            isPlayer1sTurn = !isPlayer1sTurn;
        }
        return(map);
    }

    /**
     * Handles end game conditions and determines the winner based on the number of guessed pairs.
     * Updates game state variables accordingly.
     * Checks if all pairs are guessed or if one player has more guessed pairs than the other.
     * If the game ends, sets the winner to the UUID of the player with more guessed pairs.
     */
    @Override
    protected void gameEnded() {
        isGameOver=true;
        for (boolean guessedCards : isGuessedBoard) {
            if (!guessedCards) {
                isGameOver = false;
                break;
            }
        }
        if(arePreviousCardsEqual) {
            if(isPlayer1sTurn) {
                player1GuessedCards++;
            } else {
                player2GuessedCards++;
            }
        }
        if(player1GuessedCards >= getNumberOfPairs()/2+1 ||
                player2GuessedCards >= getNumberOfPairs()/2+1) {
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

    /**
     * Handles the situation when a player leaves the game session.
     * If the game has already started, sets the winner to the opponent's UUID,
     * indicating that the leaving player forfeits the game.
     * If the game hasn't started yet and the leaving player is the first player,
     * the game ends immediately.
     * If the leaving player is the second player or the only player in the lobby,
     * removes the player from the game session, allowing another player to join.
     *
     * @param player the UUID of the player who leaves the game
     */
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

    public void setGameStarted(boolean isGameStarted) {
        this.isGameStarted=isGameStarted;
    }

    public void setPlayer2Id(UUID player2Id) {
        this.player2Id = player2Id;
    }

    public boolean isPlayer1sTurn() {
        return isPlayer1sTurn;
    }

    public boolean isGameStarted() {
        return  isGameStarted;
    }

    public boolean isPlayersTurn(UUID player) {
        return (isPlayer1sTurn && player.equals(player1Id)) ||
                (!isPlayer1sTurn && player.equals(player2Id));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiPlayer that = (MultiPlayer) o;
        return Objects.equals(playId, that.getPlayId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(playId);
    }
}
