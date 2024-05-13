package com.memo.game.service;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.model.SinglePlayer;
import com.memo.game.repo.MemoSingleGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class MemoSingleGameServiceTest {
    private MemoSingleGameService memoSingleGameService;
    @Mock
    private MemoSingleGameRepository memoSingleGameRepository;

    private SinglePlayer singlePlayer;
    private final UUID gameId1 = UUID.randomUUID();
    private final UUID gameId2 = UUID.randomUUID();
    private final UUID gameId3 = UUID.randomUUID();
    private final UUID userId1 = UUID.randomUUID();
    private final UUID userId2 = UUID.randomUUID();
    private final Pageable pageable = PageRequest.of(1, 10);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        MemoSingleGame game1 = new MemoSingleGame(gameId1, userId1, true, 10, 8, 60);
        MemoSingleGame game2 = new MemoSingleGame(gameId2, userId1, false, 0, 16, 120);
        MemoSingleGame game3 = new MemoSingleGame(gameId3, userId2, true, 15, 24, 300);
        singlePlayer = new SinglePlayer(8, 60, memoSingleGameService);

        List<MemoSingleGame> listOfGames1 = new ArrayList<>();
        listOfGames1.add(game1);
        listOfGames1.add(game2);
        when(memoSingleGameRepository.findByUserId(userId1)).thenReturn(listOfGames1);
        List<MemoSingleGame> listOfGames2 = new ArrayList<>();
        listOfGames2.add(game3);
        when(memoSingleGameRepository.findByUserId(userId2)).thenReturn(listOfGames2);

        when(memoSingleGameRepository.findByUserIdPaginated(userId1, pageable)).thenReturn(listOfGames1);
        when(memoSingleGameRepository.findByUserIdPaginated(userId2, pageable)).thenReturn(listOfGames2);

        when(memoSingleGameRepository.countByUserId(userId1)).thenReturn(listOfGames1.size());
        when(memoSingleGameRepository.countByUserId(userId2)).thenReturn(listOfGames2.size());

        when(memoSingleGameRepository.save(game1)).thenReturn(game1);
        when(memoSingleGameRepository.save(game2)).thenReturn(game2);
        when(memoSingleGameRepository.save(game3)).thenReturn(game3);

        memoSingleGameService = new MemoSingleGameService(memoSingleGameRepository);
    }

    @Test
    public void findGamesByUserIdTest() {
        List<MemoSingleGame> games1 = memoSingleGameService.findGamesByUserIdInDb(userId1);
        List<MemoSingleGame> games2 = memoSingleGameService.findGamesByUserIdInDb(userId2);

        assertThat(games1).isNotNull();
        assertThat(games1).extracting(MemoSingleGame::getUserId).containsOnly(userId1);
        assertThat(games1.size()).isEqualTo(2);
        assertThat(games2).isNotNull();
        assertThat(games2.size()).isEqualTo(1);
        assertThat(games2).extracting(MemoSingleGame::getUserId).containsOnly(userId2);
    }

    @Test
    public void getTotalGamesCountByUserIdTest() {
        assertThat(memoSingleGameService.getTotalGamesCountByUserIdFromDb(userId1)).isEqualTo(2);
        assertThat(memoSingleGameService.getTotalGamesCountByUserIdFromDb(userId2)).isEqualTo(1);
    }

    @Test
    public void findGamesByUserIdPageableTest() {
        List<MemoSingleGame> games1 = memoSingleGameService.findGamesByUserIdInDb(userId1, 1, 10);
        List<MemoSingleGame> games2 = memoSingleGameService.findGamesByUserIdInDb(userId2, 1, 10);

        assertThat(games1).isNotNull();
        assertThat(games1).extracting(MemoSingleGame::getUserId).containsOnly(userId1);
        assertThat(games1.size()).isEqualTo(2);
        assertThat(games2).isNotNull();
        assertThat(games2.size()).isEqualTo(1);
        assertThat(games2).extracting(MemoSingleGame::getUserId).containsOnly(userId2);
    }

    @Test
    public void addGameTest() {
        memoSingleGameService.addSinglePlayerToList(singlePlayer, userId1);
        memoSingleGameService.addSinglePlayerToList( new SinglePlayer(16, 120, memoSingleGameService), userId2);

        List<SinglePlayer> plays = memoSingleGameService.getPlays();
        Map<UUID, UUID> playsWithUsers = memoSingleGameService.getPlaysWithUsers();

        assertThat(plays).isNotNull();
        assertThat(plays.getFirst()).isNotNull();
        assertThat(plays.getFirst().getPlayId()).isEqualTo(singlePlayer.getPlayId());
        assertThat(playsWithUsers).isNotNull();
        assertThat(playsWithUsers.get(singlePlayer.getPlayId())).isEqualTo(userId1);
    }

    @Test
    public void getSinglePlayerByGameIdTest() {
        memoSingleGameService.addSinglePlayerToList(singlePlayer, userId1);
        SinglePlayer play = memoSingleGameService.getSinglePlayerByGameIdFromList(singlePlayer.getPlayId());
        assertThat(play).isNotNull();
        assertThat(play.getPlayId()).isEqualTo(singlePlayer.getPlayId());
    }

    @Test
    public void removeGame() {
        memoSingleGameService.addSinglePlayerToList(singlePlayer, userId2);
        memoSingleGameService.addSinglePlayerToList( new SinglePlayer(16, 120, memoSingleGameService), userId1);
        memoSingleGameService.addSinglePlayerToList( new SinglePlayer(8, 120, memoSingleGameService), userId2);

        memoSingleGameService.removeSinglePlayerFromList(singlePlayer);

        List<SinglePlayer> plays = memoSingleGameService.getPlays();
        Map<UUID, UUID> playsWithUsers = memoSingleGameService.getPlaysWithUsers();
        assertThat(plays).isNotNull();
        assertThat(plays.size()).isEqualTo(2);
        assertThat(playsWithUsers).isNotNull();
        assertThat(playsWithUsers.get(singlePlayer.getPlayId())).isEqualTo(null);
    }
}
