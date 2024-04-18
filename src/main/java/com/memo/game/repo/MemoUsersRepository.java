package com.memo.game.repo;

import com.memo.game.entity.MemoUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemoUsersRepository extends JpaRepository<MemoUsers, UUID> {
    MemoUsers findByEmail(String email);

    MemoUsers findByUserName(String userName);

    @Query("SELECT count(g) FROM MemoMultiGame g where (g.winner = :winnerId and g.pairs = :pairs)")
    Integer getWins(@Param("winnerId") String winnerId, @Param("pairs") int pairs);
}
