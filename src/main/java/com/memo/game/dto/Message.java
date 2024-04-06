package com.memo.game.dto;
import java.util.UUID;

public interface Message {
    String getType();
    UUID getGameId();
    String getContent();
}
