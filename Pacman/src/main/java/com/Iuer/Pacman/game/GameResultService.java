package com.Iuer.Pacman.game;

import com.Iuer.Pacman.entity.GameOutcome;
import com.Iuer.Pacman.entity.GameplayResult;
import com.Iuer.Pacman.entity.User;
import com.Iuer.Pacman.repository.GameplayResultRepository;
import com.Iuer.Pacman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class GameResultService {
    private final GameplayResultRepository results;
    private final UserRepository users;
    private final String gameVersion;

    public GameResultService(
            GameplayResultRepository results,
            UserRepository users,
            @Value("${app.game.version:1.0}") String gameVersion
    ) {
        this.results = results;
        this.users = users;
        this.gameVersion = gameVersion;
    }

    public GameplayResult build(PlayerState player, User user, long nowNanos, Instant endedAt) {
        GameplayResult result = new GameplayResult();
        result.setUserId(user.getId());
        result.setMapName(player.map().id());
        result.setGameMode(player.mode().id());
        result.setGameVersion(gameVersion);
        result.setScore(player.score());
        result.setDurationSeconds(player.durationSeconds(nowNanos));
        result.setOutcome(player.isCleared() ? GameOutcome.WIN : GameOutcome.LOSE);
        result.setStartedAt(player.startedAt());
        result.setEndedAt(endedAt);
        return result;
    }

    public Optional<GameplayResult> record(PlayerState player) {
        if (player.score() <= 0) return Optional.empty();
        Optional<User> user = users.findByUsername(player.username());
        if (user.isEmpty()) return Optional.empty();
        GameplayResult result = build(player, user.get(), System.nanoTime(), Instant.now());
        return Optional.of(results.save(result));
    }
}
