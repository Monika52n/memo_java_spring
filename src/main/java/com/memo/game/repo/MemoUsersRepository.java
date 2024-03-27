package com.memo.game.repo;

import com.memo.game.entity.MemoUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemoUsersRepository extends JpaRepository<MemoUsers, UUID> {
    MemoUsers findByEmail(String email);

    MemoUsers findByUserName(String userName);

}
