package com.memo.game.service;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.model.GameSaver;
import com.memo.game.repo.MemoSingleGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MemoSingleGameService implements GameSaver {
    private final Map<UUID, UUID> playsWithUsers= new HashMap<UUID, UUID>();

    private final MemoSingleGameRepository gameRepository;

    @Autowired
    public MemoSingleGameService(MemoSingleGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public MemoSingleGame saveGame(MemoSingleGame game) {
        return gameRepository.save(game);
    }

    public List<MemoSingleGame> findGamesByUserId(UUID userId) {
        return gameRepository.findByUserId(userId);
    }

    public List<MemoSingleGame> findGamesByUserId(UUID userId, int page, int size) {
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

    public void addPlayWithUser(UUID play, UUID player) {
        playsWithUsers.put(play, player);
    }

    public int getTotalGamesCountByUserId(UUID userId) {
        return gameRepository.countByUserId(userId);
    }
}
