package com.memo.game.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "memo_single_games")
public class MemoSingleGame {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "userid")
    private UUID userId;
    @JsonProperty("won")
    private boolean won;
    @JsonProperty("remainingTime")
    private int remainingTime;
    @JsonProperty("pairs")
    private int pairs;
    @JsonProperty("timeMax")
    private int timeMax;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Europe/Budapest")
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    public MemoSingleGame() {
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
    }

    public MemoSingleGame(boolean won, int remainingTime, int pairs, int timeMax) {
        this.won=won;
        this.remainingTime=remainingTime;
        this.pairs=pairs;
        this.timeMax=timeMax;
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
    }

    public MemoSingleGame(UUID id, UUID userId, boolean won,
                          int remainingTime, int pairs, int timeMax) {
        this.id=id;
        this.userId=userId;
        this.won=won;
        this.remainingTime=remainingTime;
        this.pairs=pairs;
        this.timeMax=timeMax;
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
    }

    public boolean isWon() {
        return won;
    }

    public int getPairs() {
        return pairs;
    }

    public int getTimeMax() {
        return timeMax;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public UUID getUserId() {
        return userId;
    }
}