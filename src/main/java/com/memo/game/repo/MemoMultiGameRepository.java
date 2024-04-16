package com.memo.game.repo;

import com.memo.game.entity.MemoMultiGame;
import com.memo.game.entity.MemoSingleGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemoMultiGameRepository extends JpaRepository<MemoMultiGame, UUID> {
}
