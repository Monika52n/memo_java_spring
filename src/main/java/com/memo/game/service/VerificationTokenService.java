package com.memo.game.service;

import com.memo.game.entity.MemoAuth;
import com.memo.game.repo.MemoAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private final MemoAuthRepository memoAuthRepository;

    @Autowired
    public VerificationTokenService(MemoAuthRepository memoAuthRepository) {
        this.memoAuthRepository = memoAuthRepository;
    }

    public UUID saveVerificationToken(String email) {
        MemoAuth memoAuth = new MemoAuth(email);
        memoAuthRepository.save(memoAuth);
        return  memoAuth.getVerificationToken();
    }

    public boolean verifyToken(UUID token) {
        MemoAuth user = memoAuthRepository.findByVerificationToken(token);
        if (user != null) {
            user.setVerified(true);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            memoAuthRepository.save(user);
            return true;
        }
        return false;
    }

    public void markEmailAsVerified(String email) {
        // Implement marking email as verified in the database
    }
}
