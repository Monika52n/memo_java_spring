package com.memo.game.service;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.gameModel.SinglePlayer;
import com.memo.game.repo.MemoSingleGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SinglePlayerService implements GameSaver {
    private final List<SinglePlayer> plays = new ArrayList<SinglePlayer>();
    private final Map<UUID, UUID> playsWithUsers= new HashMap<UUID, UUID>();
    private final MemoSingleGameRepository gameRepository;

    @Autowired
    public SinglePlayerService(MemoSingleGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public MemoSingleGame saveGame(MemoSingleGame game) {
        return gameRepository.save(game);
    }

    public List<MemoSingleGame> findGamesByUserIdInDb(UUID userId) {
        return gameRepository.findByUserId(userId);
    }

    public List<MemoSingleGame> findGamesByUserIdInDb(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return gameRepository.findByUserIdPaginated(userId, pageable);
    }
    @Override
    public void saveGameAfterEnded(UUID gameId, boolean won, int timeRemaining,
                  int pairs, int initialTime) {
        UUID userId = playsWithUsers.get(gameId);
        MemoSingleGame game = new MemoSingleGame(gameId, userId, won, timeRemaining, pairs, initialTime);
        saveGame(game);
    }

    public int getTotalGamesCountByUserIdFromDb(UUID userId) {
        return gameRepository.countByUserId(userId);
    }

    public void addSinglePlayerToList(SinglePlayer game, UUID userId) {
        plays.add(game);
        playsWithUsers.put(game.getPlayId(), userId);
    }

    public SinglePlayer getSinglePlayerByGameIdFromList(UUID id) {
        for (SinglePlayer play : plays) {
            if (id.equals(play.getPlayId())) {
                return play;
            }
        }
        return null;
    }

    public void removeSinglePlayerFromList(SinglePlayer singleplayer) {
        plays.remove(singleplayer);
        playsWithUsers.remove(singleplayer.getPlayId());
    }

    public List<SinglePlayer> getPlays() {
        return Collections.unmodifiableList(plays);
    }

    public Map<UUID, UUID> getPlaysWithUsers() {
        return Collections.unmodifiableMap(playsWithUsers);
    }
}
