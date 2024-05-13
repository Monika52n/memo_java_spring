package com.memo.game.service;

import com.memo.game.dto.UserNameSearcher;
import com.memo.game.entity.MemoUsers;
import com.memo.game.repo.MemoUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemoUsersService implements UserNameSearcher {
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

    @Override
    public String getUserNameById(UUID id) {
        if(id!=null && gameRepository.findById(id).isPresent()) {
            MemoUsers user = gameRepository.findById(id).get();
            return user.getUserName();
        }
        return null;
    }
}
