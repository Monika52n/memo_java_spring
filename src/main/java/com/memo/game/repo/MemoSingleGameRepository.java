package com.memo.game.repo;

import com.memo.game.entity.MemoSingleGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MemoSingleGameRepository extends JpaRepository<MemoSingleGame, UUID> {
    List<MemoSingleGame> findByUserId(UUID userId);

    @Query("SELECT g FROM MemoSingleGame g WHERE g.userId = :userId")
    List<MemoSingleGame> findByUserIdPaginated(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COUNT(g) FROM MemoSingleGame g WHERE g.userId = :userId")
    int countByUserId(@Param("userId") UUID userId);
}
