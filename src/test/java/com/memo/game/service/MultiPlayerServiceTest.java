package com.memo.game.service;

import com.memo.game.gameModel.MultiPlayer;
import com.memo.game.repo.MemoMultiGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class MultiPlayerServiceTest {
    private MultiPlayerService multiPlayerService;
    @Mock
    private MemoMultiGameRepository memoMultiGameRepository;
    private final UUID userId1 = UUID.randomUUID();
    private final UUID userId2 = UUID.randomUUID();
    private final UUID userId3 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(memoMultiGameRepository.save(any())).thenReturn(null);
        multiPlayerService = new MultiPlayerService(memoMultiGameRepository);
    }

    @Test
    void joinGameOnePlayerTest() {
        multiPlayerService.joinGame(userId1, 8);
        List<MultiPlayer> games =  multiPlayerService.getGames();
        List<MultiPlayer> gamesWithFriends =  multiPlayerService.getGamesWithFriends();

        assertThat(games).isNotNull();
        assertThat(games.size()).isEqualTo(1);
        assertThat(games.getFirst()).isNotNull();
        assertThat(games.getFirst().getPlayer1Id()).isEqualTo(userId1);
        assertThat(gamesWithFriends).isNotNull();
        assertThat(gamesWithFriends.size()).isEqualTo(0);
    }

    @Test
    void joinTwoPlayersSameModeTest() {
        multiPlayerService.joinGame(userId1, 8);
        multiPlayerService.joinGame(userId2, 8);
        List<MultiPlayer> games =  multiPlayerService.getGames();

        assertThat(games).isNotNull();
        assertThat(games.size()).isEqualTo(1);
        assertThat(games.getFirst()).isNotNull();
        assertThat(games.getFirst().getPlayer1Id()).isEqualTo(userId1);
        assertThat(games.getFirst().getPlayer2Id()).isEqualTo(userId2);
        assertThat(multiPlayerService.getGameByPlayer(userId1)).isEqualTo(multiPlayerService.getGameByPlayer(userId2));
    }

    @Test
    void joinThreePlayersTest() {
        multiPlayerService.joinGame(userId1, 8);
        multiPlayerService.joinGame(userId2, 8);
        multiPlayerService.joinGame(userId3, 8);

        List<MultiPlayer> games =  multiPlayerService.getGames();
        assertThat(games).isNotNull();
        assertThat(games.size()).isEqualTo(2);
    }

    @Test
    void joinTwoPlayersDifModeTest() {
        multiPlayerService.joinGame(userId1, 8);
        multiPlayerService.joinGame(userId2, 16);
        assertThat(multiPlayerService.getGameByPlayer(userId1)).isNotNull();
        assertThat(multiPlayerService.getGameByPlayer(userId1)).isNotEqualTo(multiPlayerService.getGameByPlayer(userId2));
    }

    @Test
    void joinIncorrectUserId() {
        MultiPlayer game = multiPlayerService.joinGame(null, 8);
        assertThat(game).isNull();
    }

    @Test
    void joinTwice() {
        multiPlayerService.joinGame(userId1, 8);
        MultiPlayer game = multiPlayerService.joinGame(userId1, 16);
        List<MultiPlayer> games =  multiPlayerService.getGames();

        assertThat(games).isNotNull();
        assertThat(games.size()).isEqualTo(1);
        assertThat(game.getNumberOfPairs()).isEqualTo(8);
    }

    @Test
    void joinWithFriendsOnePlayerTest() {
        multiPlayerService.joinGameWithFriend(userId1, 8, null);
        List<MultiPlayer> gamesWithFriends =  multiPlayerService.getGamesWithFriends();
        List<MultiPlayer> games =  multiPlayerService.getGames();

        assertThat(games).isNotNull();
        assertThat(games.size()).isEqualTo(0);
        assertThat(gamesWithFriends).isNotNull();
        assertThat(gamesWithFriends.size()).isEqualTo(1);
        assertThat(gamesWithFriends.getFirst()).isNotNull();
        assertThat(gamesWithFriends.getFirst().getPlayer1Id()).isEqualTo(userId1);
    }

    @Test
    void joinWithFriendsTwoPlayersTest() {
        MultiPlayer game = multiPlayerService.joinGameWithFriend(userId1, 8, null);
        multiPlayerService.joinGameWithFriend(userId2, 0, game.getPlayId());

        assertThat(multiPlayerService.getGameByPlayer(userId2)).isEqualTo(game);
    }

    @Test
    void joinWithFriendsIncorrectNumberOfPairs() {
        MultiPlayer game = multiPlayerService.joinGameWithFriend(userId1, 0, null);
        assertThat(game).isNull();
        assertThat(multiPlayerService.getGameByPlayer(userId1)).isNull();
    }

    @Test
    void joinWithFriendsIncorrectGameId() {
        multiPlayerService.joinGameWithFriend(userId1, 8, null);
        MultiPlayer game = multiPlayerService.joinGameWithFriend(userId2, 8, UUID.randomUUID());
        assertThat(game).isNull();
        assertThat(multiPlayerService.getGameByPlayer(userId2)).isNull();
    }

    @Test
    void joinWithFriendsIncorrectUserId() {
        MultiPlayer game = multiPlayerService.joinGameWithFriend(null, 8, null);
        assertThat(game).isNull();
    }

    @Test
    void joinTwiceWithFriends() {
        multiPlayerService.joinGameWithFriend(userId1, 8, null);
        MultiPlayer game =  multiPlayerService.joinGameWithFriend(userId1, 16, null);
        List<MultiPlayer> gamesWithFriends =  multiPlayerService.getGamesWithFriends();

        assertThat(gamesWithFriends).isNotNull();
        assertThat(gamesWithFriends.size()).isEqualTo(1);
        assertThat(game.getNumberOfPairs()).isEqualTo(8);
    }

    @Test
    void joinToGameWhereTwoAlreadyJoined() {
        UUID gameId =  multiPlayerService.joinGameWithFriend(userId1, 8, null).getPlayId();
        multiPlayerService.joinGameWithFriend(userId2, 0, gameId);

        MultiPlayer game = multiPlayerService.joinGameWithFriend(userId3, 0, gameId);
        assertThat(game).isNull();
    }

    @Test
    void leaveGameBeforeStarted() {
        multiPlayerService.joinGame(userId1, 8);
        MultiPlayer game = multiPlayerService.leaveGame(userId1);

        List<MultiPlayer> games =  multiPlayerService.getGames();
        assertThat(games.size()).isEqualTo(0);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.isGameStarted()).isFalse();
    }

    @Test
    void leaveGameAfterStarted() {
        multiPlayerService.joinGame(userId1, 8);
        multiPlayerService.joinGame(userId2, 8);
        MultiPlayer game = multiPlayerService.leaveGame(userId1);

        List<MultiPlayer> games =  multiPlayerService.getGames();
        assertThat(games.size()).isEqualTo(0);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.getWinner()).isEqualTo(userId2.toString());
    }

    @Test
    void leaveGameBeforeStartedWithFriends() {
        multiPlayerService.joinGameWithFriend(userId1, 8, null);
        MultiPlayer game = multiPlayerService.leaveGame(userId1);

        List<MultiPlayer> gamesWithFriends =  multiPlayerService.getGamesWithFriends();
        assertThat(gamesWithFriends.size()).isEqualTo(0);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.isGameStarted()).isFalse();
    }

    @Test
    void leaveGameAfterStartedWithFriends() {
        UUID gameId = multiPlayerService.joinGameWithFriend(userId1, 8, null).getPlayId();
        multiPlayerService.joinGameWithFriend(userId2, 8, gameId);
        MultiPlayer game = multiPlayerService.leaveGame(userId1);

        List<MultiPlayer> gamesWithFriends =  multiPlayerService.getGamesWithFriends();
        assertThat(gamesWithFriends.size()).isEqualTo(0);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.getWinner()).isEqualTo(userId2.toString());
    }

    @Test
    void getGameTest() {
        multiPlayerService.joinGame(userId1, 8);
        UUID gameId = multiPlayerService.joinGame(userId2, 16).getPlayId();
        assertThat(multiPlayerService.getGame(gameId)).isNotNull();
    }

    @Test
    void removeGameTest() {
        UUID gameId = multiPlayerService.joinGame(userId1, 8).getPlayId();

        multiPlayerService.removeGame(gameId);
        List<MultiPlayer> games =  multiPlayerService.getGames();
        assertThat(games.size()).isEqualTo(0);
    }
}
