package com.memo.game.repo;

import com.memo.game.entity.MemoMultiGame;
import com.memo.game.entity.MemoSingleGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemoMultiGameRepository extends JpaRepository<MemoMultiGame, UUID> {
    List<MemoMultiGame> findByPlayer1(UUID player1);
    List<MemoMultiGame> findByPlayer2(UUID player2);
}
