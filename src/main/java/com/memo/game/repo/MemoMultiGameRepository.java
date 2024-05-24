package com.memo.game.repo;

import com.memo.game.entity.MemoMultiGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemoMultiGameRepository extends JpaRepository<MemoMultiGame, UUID> {
    List<MemoMultiGame> findByPlayer1(UUID player1);
    List<MemoMultiGame> findByPlayer2(UUID player2);
    @Query("SELECT count(g) FROM MemoMultiGame g where (g.winner = :winnerId and g.pairs = :pairs)")
    Integer getWins(@Param("winnerId") String winnerId, @Param("pairs") int pairs);
}
