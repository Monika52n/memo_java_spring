package com.memo.game.gameModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.UUID;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
@ExtendWith(SpringExtension.class)
public class MultiPlayerTest {
    MultiPlayer multiPlayer;
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();

    void setBoardCardsInOrder(MultiPlayer multiplayer) {
        int[] board = new int[multiplayer.getNumberOfPairs()*2];
        int index = 0;
        for(int i=1; i<=multiplayer.getNumberOfPairs(); i++) {
            board[index++] = i;
            board[index++] = i;
        }
        multiplayer.setBoard(board);
    }

    @Test
    void constructorTest() {
        multiPlayer = new MultiPlayer(8, null, null);
        assertThat(multiPlayer).isNotNull();
        assertThat(multiPlayer.getPlayer1GuessedCards()).isEqualTo(0);
        assertThat(multiPlayer.getWinner()).isEqualTo("draw");
        assertThat(multiPlayer.getPlayer2GuessedCards()).isEqualTo(0);
        assertThat(multiPlayer.getNumberOfPairs()).isEqualTo(8);
        assertThat(multiPlayer.isGameOver()).isEqualTo(false);
        assertThat(multiPlayer.isGameStarted()).isEqualTo(false);
        assertThat(multiPlayer.getPlayId()).isNotNull();
    }

    @Test
    void constructorThrowsIllegalArgumentExceptionWhenIncorrectParams() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MultiPlayer(-1, null, null);
        });
    }

    @Test
    void flipCardThrowsIllegalArgumentExceptionWhenBothPlayersAreNull() {
        MultiPlayer multiPlayer = new MultiPlayer(8, null, null);
        assertThrows(IllegalArgumentException.class, () -> {
            multiPlayer.flipCard(null, 2);
        });
    }

    @Test
    void flipCardThrowsIllegalArgumentExceptionWhenPlayerOneIsNull() {
        MultiPlayer multiPlayer = new MultiPlayer(8, null, userId1);
        assertThrows(IllegalArgumentException.class, () -> {
            multiPlayer.flipCard(null, 2);
        });
    }

    @Test
    void flipCardThrowsIllegalArgumentExceptionWhenPlayerTwoIsNull() {
        MultiPlayer multiPlayer = new MultiPlayer(8, userId1, null);
        assertThrows(IllegalArgumentException.class, () -> {
            multiPlayer.flipCard(userId1, 2);
        });
    }
    @Test
    void flipFirstPlayerFirstCardTest() {
        multiPlayer = new MultiPlayer(8, userId1, userId2);
        setBoardCardsInOrder(multiPlayer);
        multiPlayer.setGameStarted(true);

        Map<Integer, Integer> cards = multiPlayer.flipCard(userId1, 2);
        assertThat(cards).isNotNull();
        assertThat(cards.size()).isEqualTo(1);
        assertThat(cards).containsOnly(entry(2, 2));
        assertThat(multiPlayer.isPlayer1sTurn()).isEqualTo(true);
    }

    @Test
    void flipFirstPlayerSecondCardTest() {
        multiPlayer = new MultiPlayer(8, userId1, userId2);

        setBoardCardsInOrder(multiPlayer);
        multiPlayer.setGameStarted(true);
        multiPlayer.flipCard(userId1, 2);

        Map<Integer, Integer> cards = multiPlayer.flipCard(userId1, 4);
        assertThat(cards).isNotNull();
        assertThat(cards.size()).isEqualTo(2);
        assertThat(multiPlayer.isPlayer1sTurn()).isEqualTo(false);
        assertThat(cards).containsOnly(entry(2, 2), entry(4,3));
    }

    @Test
    void flipSecondPlayerFirstCardTest() {
        multiPlayer = new MultiPlayer(8, userId1, userId2);
        setBoardCardsInOrder(multiPlayer);
        multiPlayer.setGameStarted(true);
        multiPlayer.flipCard(userId1, 2);
        multiPlayer.flipCard(userId1, 4);

        Map<Integer, Integer> cards = multiPlayer.flipCard(userId2, 5);
        assertThat(cards).isNotNull();
        assertThat(cards.size()).isEqualTo(1);
        assertThat(multiPlayer.isPlayer1sTurn()).isEqualTo(false);
        assertThat(cards).containsOnly(entry(5,3));
    }

    @Test
    void flipSecondPlayerSecondCardTest() {
        multiPlayer = new MultiPlayer(8, userId1, userId2);
        setBoardCardsInOrder(multiPlayer);
        multiPlayer.setGameStarted(true);
        multiPlayer.flipCard(userId1, 2);
        multiPlayer.flipCard(userId1, 4);
        multiPlayer.flipCard(userId2, 5);

        Map<Integer, Integer> cards = multiPlayer.flipCard(userId2, 1);
        assertThat(cards).isNotNull();
        assertThat(cards.size()).isEqualTo(2);
        assertThat(multiPlayer.isPlayer1sTurn()).isEqualTo(true);
        assertThat(cards).containsOnly(entry(1, 1), entry(5,3));
    }

    @Test
    void notStartBeforeSettingIsGameStartedTrueTest() {
        multiPlayer = new MultiPlayer(8, userId1, userId2);
        Map<Integer, Integer> cards1 = multiPlayer.flipCard(userId1, 2);
        assertThat(cards1).isNotNull();
        assertThat(cards1.size()).isEqualTo(0);
    }

    @Test
    void flipCardsWrongTurnTest() {
        multiPlayer = new MultiPlayer(8, userId1, userId2);
        multiPlayer.setGameStarted(true);

        Map<Integer, Integer> cards2 = multiPlayer.flipCard(userId2, 2);
        assertThat(cards2).isNotNull();
        assertThat(cards2.size()).isEqualTo(0);
    }

    @Test
    void flipCardsInBetweenTurnOrderTest() {
        multiPlayer = new MultiPlayer(8, userId1, userId2);
        multiPlayer.setGameStarted(true);
        multiPlayer.flipCard(userId1, 2);
        Map<Integer, Integer> cards3 = multiPlayer.flipCard(userId2, 2);
        assertThat(cards3).isNotNull();
        assertThat(cards3.size()).isEqualTo(0);
    }

    @Test
    void onePlayerWinsTest() {
        int numOfPairs = 8;
        multiPlayer = new MultiPlayer(numOfPairs, userId1, userId2);
        setBoardCardsInOrder(multiPlayer);
        multiPlayer.setGameStarted(true);
        for(int i=0; i<(numOfPairs-1)*2; i=i+2) {
            multiPlayer.flipCard(userId1, i);
            multiPlayer.flipCard(userId1, i+2);
            multiPlayer.flipCard(userId2, i);
            multiPlayer.flipCard(userId2, i+1);
        }

        assertThat(multiPlayer.isGameOver()).isEqualTo(true);
        assertThat(multiPlayer.getWinner()).isEqualTo(userId2.toString());
        assertThat(multiPlayer.getPlayer2GuessedCards()).isEqualTo(numOfPairs/2+1);
        assertThat(multiPlayer.getPlayer1GuessedCards()).isEqualTo(0);
    }

    @Test
    void drawTest() {
        int numOfPairs = 8;
        multiPlayer = new MultiPlayer(numOfPairs, userId1, userId2);
        setBoardCardsInOrder(multiPlayer);
        multiPlayer.setGameStarted(true);
        for(int i=0; i<numOfPairs*2; ) {
            multiPlayer.flipCard(userId1, i++);
            multiPlayer.flipCard(userId1, i++);
            multiPlayer.flipCard(userId2, i++);
            multiPlayer.flipCard(userId2, i++);
        }
        assertThat(multiPlayer.isGameOver()).isEqualTo(true);
        assertThat(multiPlayer.getWinner()).isEqualTo("draw");
    }

    @Test
    void flipSameCardsTest() {
        int numOfPairs = 8;
        multiPlayer = new MultiPlayer(numOfPairs, userId1, userId2);
        setBoardCardsInOrder(multiPlayer);
        multiPlayer.setGameStarted(true);
        for(int i = 0; i<3; i++) {
            multiPlayer.flipCard(userId1,0);
            multiPlayer.flipCard(userId1,1);
            multiPlayer.flipCard(userId2,0);
            multiPlayer.flipCard(userId2,1);
        }
        assertThat(multiPlayer.getPlayer1GuessedCards()).isEqualTo(1);
        assertThat(multiPlayer.getPlayer2GuessedCards()).isEqualTo(0);
    }

    @Test
    void leaveGameAfterStartingTest() {
        multiPlayer = new MultiPlayer(8, userId1, userId2);
        setBoardCardsInOrder(multiPlayer);
        multiPlayer.setGameStarted(true);
        multiPlayer.flipCard(userId1, 2);
        multiPlayer.flipCard(userId1, 4);
        multiPlayer.flipCard(userId2, 5);
        multiPlayer.flipCard(userId2, 1);

        multiPlayer.playerLeaves(userId1);
        assertThat(multiPlayer.isGameOver()).isTrue();
        assertThat(multiPlayer.getWinner()).isEqualTo(userId2.toString());
    }

    @Test
    void leaveGameBeforeStartingTest() {
        multiPlayer = new MultiPlayer(8, userId1, null);
        multiPlayer.playerLeaves(userId1);
        assertThat(multiPlayer.isGameOver()).isTrue();
        assertThat(multiPlayer.getWinner()).isEqualTo("draw");
    }
}
