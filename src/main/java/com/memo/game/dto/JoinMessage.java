package com.memo.game.dto;

import java.util.UUID;

public class JoinMessage {
    private int numOfPairs;
    private String token;
    private boolean wantToPlayWithFriend;
    private String friendRoomId;

    public int getNumOfPairs() {
        return numOfPairs;
    }

    public String getToken() {
        return token;
    }

    public boolean isWantToPlayWithFriend() {
        return wantToPlayWithFriend;
    }

    public String getFriendRoomId() {
        return friendRoomId;
    }
}