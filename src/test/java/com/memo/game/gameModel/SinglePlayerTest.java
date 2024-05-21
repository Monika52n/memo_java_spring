package com.memo.game.gameModel;

import com.memo.game.service.SinglePlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SinglePlayerTest {
    @Mock
    SinglePlayerService singlePlayerService;
    SinglePlayer singlePlayer;

    @BeforeEach
    void setUp() {
        singlePlayerService = mock(SinglePlayerService.class);
        when(singlePlayerService.saveGame(any())).thenReturn(null);
    }

    void setBoardCardsInOrder(SinglePlayer singlePlayer) {
        int[] board = new int[singlePlayer.getNumberOfPairs()*2];
        int index = 0;
        for(int i=1; i<=singlePlayer.getNumberOfPairs(); i++) {
            board[index++] = i;
            board[index++] = i;
        }
        singlePlayer.setBoard(board);
    }

    @Test
    void constructorTest() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);

        assertThat(singlePlayer).isNotNull();
        assertThat(singlePlayer.getNumOfGuessedPairs()).isEqualTo(0);
        assertThat(singlePlayer.getWon()).isEqualTo(false);
        assertThat(singlePlayer.getInitialTime()).isEqualTo(120);
        assertThat(singlePlayer.getNumberOfPairs()).isEqualTo(8);
        assertThat(singlePlayer.isGameOver()).isEqualTo(false);
    }

    @Test
    void constructorThrowsIllegalArgumentExceptionWhenNumberOfPairsIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SinglePlayer(-1, 120, singlePlayerService);
        });
    }

    @Test
    void constructorThrowsIllegalArgumentExceptionWhenInitialTimeIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SinglePlayer(8, -10, singlePlayerService);
        });
    }

    @Test
    void getFirstCardTest() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);
        setBoardCardsInOrder(singlePlayer);

        Map<Integer, Integer> cards = singlePlayer.getCard(3);
        assertThat(cards).isNotNull();
        assertThat(cards.size()).isEqualTo(1);
        assertThat(cards).containsOnly(entry(3,2));
    }

    @Test
    void getSecondCardTest() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);
        setBoardCardsInOrder(singlePlayer);
        singlePlayer.getCard(3);

        Map<Integer, Integer> cards = singlePlayer.getCard(0);
        assertThat(cards).isNotNull();
        assertThat(cards.size()).isEqualTo(2);
        assertThat(cards).containsOnly(entry(3,2), entry(0,1));
    }

    @Test
    void getArePreviousCardsEqualTest() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);
        setBoardCardsInOrder(singlePlayer);

        singlePlayer.getCard(0);
        singlePlayer.getCard(1);
        assertThat(singlePlayer.getArePreviousCardsequal()).isEqualTo(true);
        singlePlayer.getCard(2);
        singlePlayer.getCard(4);
        assertThat(singlePlayer.getArePreviousCardsequal()).isEqualTo(false);
    }

    @Test
    void winGameTest() {
        int numberOfPairs = 8;
        singlePlayer = new SinglePlayer(numberOfPairs,120, singlePlayerService);
        setBoardCardsInOrder(singlePlayer);

        for(int i =0; i<numberOfPairs*2; i++) {
            singlePlayer.getCard(i);
        }

        assertThat(singlePlayer.isGameOver()).isEqualTo(true);
        assertThat(singlePlayer.getWon()).isEqualTo(true);
    }

    @Test
    void getCardThrowsArrayIndexOutOfBoundsExceptionWhenIncorrectIndex() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            singlePlayer.getCard(-1);
        });
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            singlePlayer.getCard(16);
        });
    }

    @Test
    void getCardThrowsIllegalArgumentExceptionWhenSameCard() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);
        singlePlayer.getCard(0);
        assertThrows(IllegalArgumentException.class, () -> {
            singlePlayer.getCard(0);
        });
    }

    @Test
    void getCardGettingSamePairs() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);
        setBoardCardsInOrder(singlePlayer);
        for(int i = 0; i<3; i++) {
            singlePlayer.getCard(0);
            singlePlayer.getCard(1);
        }
        assertThat(singlePlayer.getNumOfGuessedPairs()).isEqualTo(1);
    }

    @Test
    void isTimerRunningTest() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);
        assertThat(singlePlayer.getTimeRemaining()).isLessThanOrEqualTo(120);
    }

    @Test
    void leaveGameTest() {
        singlePlayer = new SinglePlayer(8,120, singlePlayerService);
        singlePlayer.getCard(0);
        singlePlayer.leaveGame();
        assertThat(singlePlayer.isGameOver()).isTrue();
        assertThat(singlePlayer.getTimeRemaining()).isEqualTo(0);
    }
}
