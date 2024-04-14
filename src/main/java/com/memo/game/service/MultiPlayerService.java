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
        if(player==null || numberOfPairs<=0) return null;
        Optional<MultiPlayer> gameRet =
                (games.stream().filter(game -> player.equals(game.getPlayer1Id())
                || (game.getPlayer2Id() != null && player.equals(game.getPlayer2Id()))).findFirst());
        if (gameRet.isPresent()) {
            return gameRet.get();
        }

        for (MultiPlayer game : games) {
            if (game.getPlayer1Id() != null && game.getPlayer2Id() == null
                    && game.getNumberOfPairs()==numberOfPairs) {
                game.setPlayer2Id(player);
                game.setGameStarted(true);
                //game.setGameState(GameState.PLAYER1_TURN);
                printDebug();
                return game;
            }
        }

        MultiPlayer game = new MultiPlayer(numberOfPairs, player, null);
        games.add(game);
        waitingPlayers.add(player);

        printDebug();
        return game;
    }

    private void printDebug() {
        System.out.println(waitingPlayers);
        for(MultiPlayer gameP : games) {
            System.out.print(gameP.toString() + " ");
        }
        System.out.print("\n");
    }
    public synchronized MultiPlayer leaveGame(UUID player) {
        MultiPlayer game = getGameByPlayer(player);
        if (game != null) {
            waitingPlayers.remove(player);
            game.playerLeaves(player);
            if(game.isGameOver()) {
                games.remove(game);
            }
            return game;
        }
        return null;
    }

    public MultiPlayer getGameByPlayer(UUID player) {
        if(player==null) return null;
        return games.stream().filter(game -> player.equals(game.getPlayer1Id())
                || (game.getPlayer2Id() != null &&
                player.equals(game.getPlayer2Id()))).findFirst().orElse(null);
    }

    public MultiPlayer getGame(UUID gameId) {
        if(gameId==null) return null;
        return games.stream().filter(game -> gameId.equals(game.getPlayId())).findFirst().orElse(null);
    }

    public void removeGame(UUID gameId) {
        MultiPlayer game = getGame(gameId);
        games.remove(game);
    }
}
