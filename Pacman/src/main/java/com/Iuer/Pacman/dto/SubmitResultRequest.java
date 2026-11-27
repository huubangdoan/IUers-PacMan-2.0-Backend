package com.Iuer.Pacman.dto;

import com.Iuer.Pacman.entity.GameOutcome;
import java.time.Instant;

public record SubmitResultRequest(
        String map,
        String mode,
        String version,
        int score,
        int durationSeconds,
        GameOutcome outcome,
        Instant startedAt,
        Instant endedAt) {}