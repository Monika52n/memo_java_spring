package com.memo.game.repo;

import com.memo.game.entity.MemoUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemoUserRepository extends JpaRepository<MemoUser, UUID> {
    MemoUser findByEmail(String email);
    MemoUser findByUserName(String userName);
}
