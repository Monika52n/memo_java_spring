package com.memo.game.service;

import com.memo.game.entity.MemoSingleGame;
import com.memo.game.gameModel.SinglePlayer;
import com.memo.game.repo.MemoSingleGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service class for managing single-player game sessions and single-player games in the database.
 * Implements the GameSaver interface to handle saving game data to the repository.
 */
@Service
public class SinglePlayerService implements GameSaver {
    /**
     * A list of active single-player games.
     */
    private final List<SinglePlayer> plays = new ArrayList<SinglePlayer>();

    /**
     * A map associating single-player game IDs with user IDs.
     * The key is the game ID, and the value is the user ID.
     */
    private final Map<UUID, UUID> playsWithUsers= new HashMap<UUID, UUID>();
    private final MemoSingleGameRepository gameRepository;

    @Autowired
    public SinglePlayerService(MemoSingleGameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Saves a single-player game to the database.
     *
     * @param game the game to be saved
     * @return the saved game entity
     */
    public MemoSingleGame saveGame(MemoSingleGame game) {
        return gameRepository.save(game);
    }

    /**
     * Finds all single-player games by user ID.
     *
     * @param userId the UUID of the user
     * @return a list of single-player games associated with the user
     */
    public List<MemoSingleGame> findGamesByUserIdInDb(UUID userId) {
        return gameRepository.findByUserId(userId);
    }

    /**
     * Finds paginated single-player games by user ID.
     *
     * @param userId the UUID of the user
     * @param page the page number to retrieve
     * @param size the number of items per page
     * @return a list of single-player games for the specified page
     */
    public List<MemoSingleGame> findGamesByUserIdInDb(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return gameRepository.findByUserIdPaginated(userId, pageable);
    }

    /**
     * Saves a single-player game after it has ended.
     *
     * @param gameId the UUID of the game
     * @param won whether the game was won or not
     * @param timeRemaining the time remaining at the end of the game
     * @param pairs the number of pairs in the game
     * @param initialTime the initial time allocated for the game
     */
    @Override
    public void saveGameAfterEnded(UUID gameId, boolean won, int timeRemaining,
                  int pairs, int initialTime) {
        UUID userId = playsWithUsers.get(gameId);
        MemoSingleGame game = new MemoSingleGame(gameId, userId, won, timeRemaining, pairs, initialTime);
        saveGame(game);
    }

    /**
     * Gets the total number of single-player games by user ID.
     *
     * @param userId the UUID of the user
     * @return the total number of games associated with the user
     */
    public int getTotalGamesCountByUserIdFromDb(UUID userId) {
        return gameRepository.countByUserId(userId);
    }

    /**
     * Adds a single-player game to the active games list.
     *
     * @param game the single-player game to add
     * @param userId the UUID of the user associated with the game
     */
    public void addSinglePlayerToList(SinglePlayer game, UUID userId) {
        plays.add(game);
        playsWithUsers.put(game.getPlayId(), userId);
    }

    /**
     * Gets a single-player game from the active games list by game ID.
     *
     * @param id the UUID of the game
     * @return the single-player game associated with the specified ID, or null if not found
     */
    public SinglePlayer getSinglePlayerByGameIdFromList(UUID id) {
        for (SinglePlayer play : plays) {
            if (id.equals(play.getPlayId())) {
                return play;
            }
        }
        return null;
    }

    /**
     * Removes a single-player game from the active games list.
     *
     * @param singleplayer the single-player game to remove
     */
    public void removeSinglePlayerFromList(SinglePlayer singleplayer) {
        plays.remove(singleplayer);
        playsWithUsers.remove(singleplayer.getPlayId());
    }

    public List<SinglePlayer> getPlays() {
        return Collections.unmodifiableList(plays);
    }

    public Map<UUID, UUID> getPlaysWithUsers() {
        return Collections.unmodifiableMap(playsWithUsers);
    }
}
