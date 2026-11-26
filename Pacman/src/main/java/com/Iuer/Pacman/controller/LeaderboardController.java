package com.Iuer.Pacman.controller;

import com.Iuer.Pacman.dto.LeaderboardEntry;
import com.Iuer.Pacman.service.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    // GET /api/leaderboard?map=classic&mode=solo&version=1.0   (cả ba tham số đều tùy chọn)
    @GetMapping
    public List<LeaderboardEntry> top10(
            @RequestParam(name = "map", required = false) String map,
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "version", required = false) String version) {
        return leaderboardService.top10(map, mode, version);
    }
}