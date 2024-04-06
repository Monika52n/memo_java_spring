package com.memo.game.service;

import com.memo.game.model.MultiPlayer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MultiPlayerService {
    private final List<MultiPlayer> games = new ArrayList<MultiPlayer>();
    private final List<UUID> waitingPlayers = new ArrayList<UUID>();

    public synchronized MultiPlayer joinGame(UUID player, int numberOfPairs) {
        Optional<MultiPlayer> gameRet =
                (games.stream().filter(game -> game.getPlayer1Id().equals(player)
                || (game.getPlayer2Id() != null && game.getPlayer2Id().equals(player))).findFirst());
        if (gameRet.isPresent()) {
            return gameRet.get();
        }

        for (MultiPlayer game : games) {
            if (game.getPlayer1Id() != null && game.getPlayer2Id() == null
                    && game.getNumberOfPairs()==numberOfPairs) {
                game.setPlayer2Id(player);
                //game.setGameState(GameState.PLAYER1_TURN);
                return game;
            }
        }

        MultiPlayer game = new MultiPlayer(numberOfPairs, player, null);
        games.add(game);
        waitingPlayers.add(player);
        return game;
    }

    public synchronized MultiPlayer leaveGame(UUID player) {
        UUID gameId = getGameByPlayer(player) != null ? getGameByPlayer(player).getPlayId() : null;
        if (gameId != null) {
            waitingPlayers.remove(player);
            MultiPlayer game = getGame(gameId);
            game.playerLeaves(player);
            if(game.getIsGameOver()) {
                games.remove(game);
            }
            return game;
        }
        return null;
    }

    public MultiPlayer getGameByPlayer(UUID player) {
        return games.stream().filter(game -> game.getPlayer2Id().equals(player)
                || (game.getPlayer2Id() != null &&
                game.getPlayer2Id().equals(player))).findFirst().orElse(null);
    }

    public MultiPlayer getGame(UUID gameId) {
        return games.stream().filter(game -> game.getPlayId().equals(gameId)).findFirst().orElse(null);
    }
}
