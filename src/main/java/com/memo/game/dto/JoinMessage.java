package com.memo.game.dto;

import java.util.UUID;

public class JoinMessage {
    private int numOfPairs;
    private String token;

    public int getNumOfPairs() {
        return numOfPairs;
    }

    public String getToken() {
        return token;
    }
}