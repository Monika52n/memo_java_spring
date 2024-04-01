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

    /*public synchronized MultiPlayer leaveGame(UUID player) {


        String gameId = getGameByPlayer(player) != null ? getGameByPlayer(player).getGameId() : null;
        if (gameId != null) {
            waitingPlayers.remove(player);
            TicTacToe game = games.get(gameId);
            if (player.equals(game.getPlayer1())) {
                if (game.getPlayer2() != null) {
                    game.setPlayer1(game.getPlayer2());
                    game.setPlayer2(null);
                    game.setGameState(GameState.WAITING_FOR_PLAYER);
                    game.setBoard(new String[3][3]);
                    waitingPlayers.put(game.getPlayer1(), game.getGameId());
                } else {
                    games.remove(gameId);
                    return null;
                }
            } else if (player.equals(game.getPlayer2())) {
                game.setPlayer2(null);
                game.setGameState(GameState.WAITING_FOR_PLAYER);
                game.setBoard(new String[3][3]);
                waitingPlayers.put(game.getPlayer1(), game.getGameId());
            }
            return game;
        }
        return null;
    }*/

    public MultiPlayer getGameByPlayer(UUID player) {
        return games.stream().filter(game -> game.getPlayer2Id().equals(player)
                || (game.getPlayer2Id() != null &&
                game.getPlayer2Id().equals(player))).findFirst().orElse(null);
    }

    public MultiPlayer getGame(UUID gameId) {
        return games.stream().filter(game -> game.getPlayId().equals(gameId)).findFirst().orElse(null);
    }
}
