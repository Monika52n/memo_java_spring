package com.memo.game.service;

import com.memo.game.entity.MemoUsers;
import com.memo.game.repo.MemoUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemoUsersService {
    private final MemoUsersRepository gameRepository;

    @Autowired
    public MemoUsersService(MemoUsersRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public MemoUsers saveUser(MemoUsers user) {
        return  gameRepository.save(user);
    }

    public MemoUsers getByEmail(String email) {
        return gameRepository.findByEmail(email);
    }

    public MemoUsers getByUserName(String username) {
        return gameRepository.findByUserName(username);
    }

    public boolean signIn(UUID userId) {
        Optional<MemoUsers> userOptional = gameRepository.findById(userId);
        if (userOptional.isPresent()) {
            MemoUsers user = userOptional.get();
            user.setSignedIn(true);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            gameRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

    public boolean signOut(UUID userId) {
        Optional<MemoUsers> userOptional = gameRepository.findById(userId);
        if (userOptional.isPresent()) {
            MemoUsers user = userOptional.get();
            user.setSignedIn(false);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            gameRepository.save(user);
            return true;
        } else {
            return false;
        }
    }

}
