package com.Iuer.Pacman.entity;
import java.util.UUID; 
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "gameplay_results")
public class GameplayResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "map_name", nullable = false)
    private String mapName;

    @Column(name = "game_mode", nullable = false)
    private String gameMode;

    @Column(name = "game_version", nullable = false)
    private String gameVersion;

    @Column(nullable = false)
    private int score;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GameOutcome outcome;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at", nullable = false)
    private Instant endedAt;

    public Long getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }
    public String getGameVersion() { return gameVersion; }
    public void setGameVersion(String gameVersion) { this.gameVersion = gameVersion; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public GameOutcome getOutcome() { return outcome; }
    public void setOutcome(GameOutcome outcome) { this.outcome = outcome; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }
}