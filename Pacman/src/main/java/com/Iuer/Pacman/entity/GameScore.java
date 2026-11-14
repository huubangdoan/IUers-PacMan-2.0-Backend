package com.Iuer.Pacman.entity;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "game_scores", indexes = {
    @Index(name = "idx_game_scores_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
public class GameScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "finished_at", nullable = false, updatable = false)
    private Instant finishedAt = Instant.now();

    public GameScore(String username, Integer score) {
        this.username = username;
        this.score = score;
        this.finishedAt = Instant.now();
    }
}
