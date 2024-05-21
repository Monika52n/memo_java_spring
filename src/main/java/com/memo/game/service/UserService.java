package com.memo.game.service;

import com.memo.game.entity.MemoUser;
import com.memo.game.repo.MemoUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements UserNameSearcher {
    private final MemoUserRepository gameRepository;

    @Autowired
    public UserService(MemoUserRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public MemoUser saveUser(MemoUser user) {
        return  gameRepository.save(user);
    }

    public MemoUser getByEmail(String email) {
        return gameRepository.findByEmail(email);
    }

    public MemoUser getByUserName(String username) {
        return gameRepository.findByUserName(username);
    }

    @Override
    public String getUserNameById(UUID id) {
        if(id!=null && gameRepository.findById(id).isPresent()) {
            MemoUser user = gameRepository.findById(id).get();
            return user.getUserName();
        }
        return null;
    }
}
