package com.memo.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "memo_multi_games")
public class MemoMultiGame {
    @Id
    private UUID id;
    private UUID player1;
    private UUID player2;
    private String winner;
    private Integer pairs;
    @Column(name = "player1_guessed_cards")
    private Integer player1GuessedCards;
    @Column(name = "player2_guessed_cards")
    private Integer player2GuessedCards;
    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    public MemoMultiGame() {
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
    }

    public MemoMultiGame(UUID id, UUID player1, UUID player2, String winner, Integer pairs,
                         Integer player1GuessedCards, Integer player2GuessedCards) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.winner = winner;
        this.pairs = pairs;
        this.player1GuessedCards = player1GuessedCards;
        this.player2GuessedCards = player2GuessedCards;
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
    }
}
