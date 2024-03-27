package com.memo.game.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
public class MemoAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID verificationToken;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private boolean verified;
    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    public MemoAuth() {
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
        this.verified=false;
    }

    public  MemoAuth(String email) {
        this.email = email;
        this.verified=false;
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
    }

    public UUID getVerificationToken() {
        return verificationToken;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
