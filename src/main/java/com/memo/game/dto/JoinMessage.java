package com.memo.game.dto;

import java.util.UUID;

public class JoinMessage implements Message {
    private String type;
    private UUID gameId;
    private UUID player;
    private int numOfPairs;
    private String content;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public UUID getGameId() {
        return gameId;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public int getNumOfPairs() {
        return numOfPairs;
    }
}