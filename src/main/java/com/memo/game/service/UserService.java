package com.memo.game.service;

import com.memo.game.entity.MemoUser;
import com.memo.game.repo.MemoUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service class for managing user operations.
 * Implements UserNameSearcher to provide search operations based on usernames.
 */
@Service
public class UserService implements UserNameSearcher {
    private final MemoUserRepository gameRepository;

    @Autowired
    public UserService(MemoUserRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Saves a user to the database.
     *
     * This method saves the provided user object to the database using the game repository.
     * It returns the saved user object.
     *
     * @param user the user object to be saved
     * @return the saved user object
     */
    public MemoUser saveUser(MemoUser user) {
        return  gameRepository.save(user);
    }

    /**
     * Retrieves a user by email from the database.
     *
     * This method retrieves a user with the specified email from the database using the game repository.
     * If a user with the given email exists, it returns the user object; otherwise, it returns null.
     *
     * @param email the email of the user to retrieve
     * @return the user object with the specified email, or null if not found
     */
    public MemoUser getByEmail(String email) {
        return gameRepository.findByEmail(email);
    }

    /**
     * Retrieves a user by username from the database.
     *
     * This method retrieves a user with the specified username from the database using the game repository.
     * If a user with the given username exists, it returns the user object; otherwise, it returns null.
     *
     * @param username the username of the user to retrieve
     * @return the user object with the specified username, or null if not found
     */
    public MemoUser getByUserName(String username) {
        return gameRepository.findByUserName(username);
    }

    /**
     * Retrieves the username of a user by their ID from the database.
     *
     * This method retrieves the username of a user with the specified ID from the database using the game repository.
     * If a user with the given ID exists, it returns their username; otherwise, it returns null.
     *
     * @param id the ID of the user
     * @return the username of the user with the specified ID, or null if not found
     */
    @Override
    public String getUserNameById(UUID id) {
        if(id!=null && gameRepository.findById(id).isPresent()) {
            MemoUser user = gameRepository.findById(id).get();
            return user.getUserName();
        }
        return null;
    }
}
