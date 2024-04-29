package com.memo.game.service;

import com.memo.game.entity.MemoMultiGame;
import com.memo.game.model.MultiPlayer;
import com.memo.game.repo.MemoMultiGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MultiPlayerService {
    private final List<MultiPlayer> games = new ArrayList<MultiPlayer>();
    private final List<MultiPlayer> gamesWithFriends = new ArrayList<>();

    private final MemoMultiGameRepository memoMultiGameRepository;

    @Autowired
    public MultiPlayerService(MemoMultiGameRepository memoMultiGameRepository) {
        this.memoMultiGameRepository = memoMultiGameRepository;
    }

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

    public MultiPlayer getGame(UUID gameId) {
        debug();
        if(gameId==null) return null;
        MultiPlayer gameRandom = games.stream().filter(game -> gameId.equals(game.getPlayId())).findFirst().orElse(null);
        if(gameRandom!=null) {
            return gameRandom;
        }
        return gamesWithFriends.stream().filter(game -> gameId.equals(game.getPlayId())).findFirst().orElse(null);
    }

    public void removeGame(UUID gameId) {
        MultiPlayer game = getGame(gameId);
        games.remove(game);
        gamesWithFriends.remove(game);
    }

    public void debug() {
        for(MultiPlayer game : games) {
            System.out.println(game);
        }
        for(MultiPlayer game : gamesWithFriends) {
            System.out.println(game);
        }
    }

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
}
