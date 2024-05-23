package com.memo.game.service;

import com.memo.game.entity.MemoMultiGame;
import com.memo.game.gameModel.MultiPlayer;
import com.memo.game.repo.MemoMultiGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service class that manages multiplayer games.
 * Handles operations such as joining games, leaving games,
 * retrieving game information, and saving game results.
 */
@Service
public class MultiPlayerService {
    /**
     * A list of active multiplayer games where players are matched randomly.
     */
    private final List<MultiPlayer> games = new ArrayList<MultiPlayer>();

    /**
     * A list of active multiplayer games where players are playing with friends.
     */
    private final List<MultiPlayer> gamesWithFriends = new ArrayList<>();
    private final MemoMultiGameRepository memoMultiGameRepository;

    @Autowired
    public MultiPlayerService(MemoMultiGameRepository memoMultiGameRepository) {
        this.memoMultiGameRepository = memoMultiGameRepository;
    }

    /**
     * Allows a player to join a game with a friend.
     *
     * If the player is already in a game, that game is returned. If the game ID is provided,
     * the player will join that specific game if it exists and the second player slot is available.
     * Otherwise, a new game is created with the specified number of pairs.
     *
     * @param player the UUID of the player joining the game
     * @param numberOfPairs the number of pairs in the game
     * @param gameId the UUID of the game to join (optional)
     * @return the game the player has joined or created, or null if the player or game parameters are invalid
     */
    public synchronized MultiPlayer joinGameWithFriend(UUID player, int numberOfPairs, UUID gameId) {
        if(player==null) return null;
        MultiPlayer gameRet = getGameByPlayer(player);
        if (gameRet!=null) {
            return gameRet;
        }

        if(gameId!=null) {
            MultiPlayer game = getGame(gameId);
            if(game==null) return null;
            if (game.getPlayer1Id() != null && game.getPlayer2Id() == null) {
                game.setPlayer2Id(player);
                game.setGameStarted(true);
                return game;
            }
        }

        if(numberOfPairs<=0) return null;
        MultiPlayer game = new MultiPlayer(numberOfPairs, player, null);
        gamesWithFriends.add(game);
        return game;
    }

    /**
     * Allows a player to join a random game.
     *
     * If the player is already in a game, that game is returned. If a suitable game is found,
     * the player joins that game. Otherwise, a new game is created with the specified number of pairs.
     *
     * @param player the UUID of the player joining the game
     * @param numberOfPairs the number of pairs in the game
     * @return the game the player has joined or created, or null if the player or number of pairs are invalid
     */
    public synchronized MultiPlayer joinGame(UUID player, int numberOfPairs) {
        if(player==null || numberOfPairs<=0) return null;
        MultiPlayer gameRet = getGameByPlayer(player);
        if (gameRet!=null) {
            return gameRet;
        }

        for (MultiPlayer game : games) {
            if (game.getPlayer1Id() != null && game.getPlayer2Id() == null
                    && game.getNumberOfPairs()==numberOfPairs) {
                game.setPlayer2Id(player);
                game.setGameStarted(true);
                return game;
            }
        }

        MultiPlayer game = new MultiPlayer(numberOfPairs, player, null);
        games.add(game);
        return game;
    }

    /**
     * Allows a player to leave a game.
     *
     * If the player is in a game, they leave the game. If the game is over, and it was started,
     * it is saved and then removed from the active games list.
     *
     * @param player the UUID of the player leaving the game
     * @return the game the player has left, or null if the player was not in a game
     */
    public synchronized MultiPlayer leaveGame(UUID player) {
        MultiPlayer game = getGameByPlayer(player);
        if (game != null) {
            game.playerLeaves(player);
            if(game.isGameOver()) {
                if(game.isGameStarted()) {
                    saveGame(game);
                }
                removeGame(game.getPlayId());
            }
            return game;
        }
        return null;
    }

    /**
     * Retrieves the game a player is currently in.
     *
     * Searches both random games and games with friends to find the game the specified player is in.
     *
     * @param player the UUID of the player
     * @return the game the player is in, or null if the player is not in any game
     */
    public MultiPlayer getGameByPlayer(UUID player) {
        if(player==null) return null;
        MultiPlayer gameRandom =
                games.stream().filter(game -> player.equals(game.getPlayer1Id())
                || (game.getPlayer2Id() != null &&
                player.equals(game.getPlayer2Id()))).findFirst().orElse(null);
        if(gameRandom!=null) {
            return gameRandom;
        }
        return gamesWithFriends.stream().filter(game -> player.equals(game.getPlayer1Id())
                || (game.getPlayer2Id() != null &&
                player.equals(game.getPlayer2Id()))).findFirst().orElse(null);
    }

    /**
     * Retrieves a game by its ID.
     *
     * Searches both random games and games with friends to find the game with the specified ID.
     *
     * @param gameId the UUID of the game
     * @return the game with the specified ID, or null if no such game exists
     */
    public MultiPlayer getGame(UUID gameId) {
        if(gameId==null) return null;
        MultiPlayer gameRandom = games.stream().filter(game -> gameId.equals(game.getPlayId())).findFirst().orElse(null);
        if(gameRandom!=null) {
            return gameRandom;
        }
        return gamesWithFriends.stream().filter(game -> gameId.equals(game.getPlayId())).findFirst().orElse(null);
    }

    /**
     * Removes a game by its ID.
     *
     * Finds the game with the specified ID and removes it from the active games list.
     *
     * @param gameId the UUID of the game to be removed
     */
    public void removeGame(UUID gameId) {
        MultiPlayer game = getGame(gameId);
        games.remove(game);
        gamesWithFriends.remove(game);
    }

    /**
     * Saves the game to the database.
     *
     * Converts the game to a MemoMultiGame entity and saves it using the memoMultiGameRepository.
     *
     * @param game the game to be saved
     */
    public void saveGame(MultiPlayer game) {
        MemoMultiGame memoMultiGame = new MemoMultiGame(
                game.getPlayId(),
                game.getPlayer1Id(),
                game.getPlayer2Id(),
                game.getWinner(),
                game.getNumberOfPairs(),
                game.getPlayer1GuessedCards(),
                game.getPlayer2GuessedCards()
        );
        memoMultiGameRepository.save(memoMultiGame);
    }

    public List<MultiPlayer> getGames() {
        return Collections.unmodifiableList(games);
    }
    public List<MultiPlayer> getGamesWithFriends() {
        return Collections.unmodifiableList(gamesWithFriends);
    }
}
