package com.memo.game;

import com.memo.game.model.MultiPlayer;
import com.memo.game.model.SinglePlayer;
import com.memo.game.service.MemoSingleGameService;
import com.memo.game.service.MemoUsersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class GameApplicationTests {

	@Autowired
	MemoSingleGameService memoSingleGameService;
	@Autowired
	MemoUsersService memoUsersService;
	SinglePlayer singlePlayer;
	MultiPlayer multiPlayer;

	@Test
	void contextLoads() {
	}

	@Test
	void singlePlayerConstructorTest() {
		singlePlayer = new SinglePlayer(8,120, memoSingleGameService);

		assertThat(singlePlayer).isNotNull();
		assertThat(singlePlayer.getNumOfGuessedPairs()).isEqualTo(0);
		assertThat(singlePlayer.getWon()).isEqualTo(false);
		assertThat(singlePlayer.getInitialTime()).isEqualTo(120);
		assertThat(singlePlayer.getNumberOfPairs()).isEqualTo(8);
		assertThat(singlePlayer.isGameOver()).isEqualTo(false);
	}

	@Test
	void singlePlayerConstructorThrowsIllegalArgumentExceptionWhenIncorrectParams() {
		assertThrows(IllegalArgumentException.class, () -> {
			new SinglePlayer(-1, 120, memoSingleGameService);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			new SinglePlayer(8, -10, memoSingleGameService);
		});
	}

	@Test
	void singlePlayerGetCardTest() {
		singlePlayer = new SinglePlayer(8,120, memoSingleGameService);
		Set<Integer> set = new HashSet<>();
		set.add(3);

		Map<Integer, Integer> cards1 = singlePlayer.getCard(3);
		assertThat(cards1).isNotNull();
		assertThat(cards1.size()).isEqualTo(1);
		assertThat(cards1.keySet()).isEqualTo(set);

		set.add(0);
		Map<Integer, Integer> cards2 = singlePlayer.getCard(0);
		assertThat(cards2).isNotNull();
		assertThat(cards2.size()).isEqualTo(2);
		assertThat(cards2.keySet()).isEqualTo(set);
	}

	@Test
	void singlePlayerGetCardThrowsIllegalArgumentExceptionWhenIncorrectIndex() {
		singlePlayer = new SinglePlayer(8,120, memoSingleGameService);

		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			singlePlayer.getCard(-1);
		});
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			singlePlayer.getCard(16);
		});
	}

	@Test
	void singlePlayerIsTimerRunningTest() {
		singlePlayer = new SinglePlayer(8,120, memoSingleGameService);
		assertThat(singlePlayer.getTimeRemaining()).isLessThanOrEqualTo(120);
	}

	@Test
	void multiPlayerConstructorTest() {
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
	void multiPlayerConstructorThrowsIllegalArgumentExceptionWhenIncorrectParams() {
		assertThrows(IllegalArgumentException.class, () -> {
			new MultiPlayer(-1, null, null);
		});
	}

	@Test
	void multiPlayerGetCardThrowsIllegalArgumentExceptionWhenNoPlayers() {
		multiPlayer = new MultiPlayer(8, null, null);
		assertThrows(IllegalArgumentException.class, () -> {
			multiPlayer.getCard(null, 2);
		});
	}

	/*@Test
	void userTest() {
		memoUsersService.saveUser()
	}*/
}
