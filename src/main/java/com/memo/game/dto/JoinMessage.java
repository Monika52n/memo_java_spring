package com.memo.game.dto;

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

    public void setNumOfPairs(int numOfPairs) {
        this.numOfPairs = numOfPairs;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setWantToPlayWithFriend(boolean wantToPlayWithFriend) {
        this.wantToPlayWithFriend = wantToPlayWithFriend;
    }

    public void setFriendRoomId(String friendRoomId) {
        this.friendRoomId = friendRoomId;
    }
}