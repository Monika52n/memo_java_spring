package com.memo.game.repo;

import com.memo.game.entity.MemoAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemoAuthRepository extends JpaRepository<MemoAuth, UUID> {
    MemoAuth findByVerificationToken(UUID verificationToken);
}
