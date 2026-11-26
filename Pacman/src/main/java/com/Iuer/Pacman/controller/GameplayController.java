package com.Iuer.Pacman.controller;

import com.Iuer.Pacman.dto.SubmitResultRequest;
import com.Iuer.Pacman.entity.GameplayResult;
import com.Iuer.Pacman.entity.User;
import com.Iuer.Pacman.repository.GameplayResultRepository;
import com.Iuer.Pacman.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameplayController {

    private static final int MAX_SCORE = 1_000_000;        // đổi theo game của bạn
    private static final int MAX_DURATION_SECONDS = 24 * 3600;
    private static final long TOLERANCE_SECONDS = 5;

    private final GameplayResultRepository results;
    private final UserRepository users;

    public GameplayController(GameplayResultRepository results, UserRepository users) {
        this.results = results;
        this.users = users;
    }

    @PostMapping("/results")
    public ResponseEntity<?> submit(@RequestBody SubmitResultRequest req, Authentication auth) {
        String error = validate(req);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        User user = users.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Người dùng không tồn tại"));
        }

        GameplayResult g = new GameplayResult();
        g.setUserId(user.getId());                     // lấy từ token, không lấy từ client
        g.setMapName(req.map().trim());
        g.setGameMode(req.mode().trim());
        g.setGameVersion(req.version().trim());
        g.setScore(req.score());
        g.setDurationSeconds(req.durationSeconds());
        g.setOutcome(req.outcome());
        g.setStartedAt(req.startedAt());
        g.setEndedAt(req.endedAt());
        results.save(g);

        return ResponseEntity.status(201).body(Map.of("id", g.getId()));
    }

    private String validate(SubmitResultRequest r) {
        if (isBlank(r.map()) || isBlank(r.mode()) || isBlank(r.version())) return "Thiếu map, mode hoặc version";
        if (r.outcome() == null || r.startedAt() == null || r.endedAt() == null) return "Thiếu outcome hoặc thời gian";
        if (r.score() < 0 || r.score() > MAX_SCORE) return "Điểm không hợp lệ";
        if (r.durationSeconds() <= 0 || r.durationSeconds() > MAX_DURATION_SECONDS) return "Thời lượng không hợp lệ";
        if (r.endedAt().isBefore(r.startedAt())) return "endedAt trước startedAt";
        if (r.endedAt().isAfter(Instant.now().plusSeconds(60))) return "endedAt ở tương lai";
        long actual = Duration.between(r.startedAt(), r.endedAt()).getSeconds();
        if (Math.abs(actual - r.durationSeconds()) > TOLERANCE_SECONDS) return "Thời lượng không khớp với thời gian bắt đầu/kết thúc";
        return null;
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}