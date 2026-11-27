package com.Iuer.Pacman.service;

import com.Iuer.Pacman.dto.LeaderboardEntry;
import com.Iuer.Pacman.entity.User;
import com.Iuer.Pacman.repository.GameplayResultRepository;
import com.Iuer.Pacman.repository.GameplayResultRepository.LeaderboardRow;
import com.Iuer.Pacman.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    public static final int TOP_N = 10;

    private final GameplayResultRepository results;
    private final UserRepository users;

    public LeaderboardService(GameplayResultRepository results, UserRepository users) {
        this.results = results;
        this.users = users;
    }

    public List<LeaderboardEntry> top10(String map, String mode, String version) {
        List<LeaderboardRow> rows = results.findTopPlayers(
                blankToNull(map), blankToNull(mode), blankToNull(version), TOP_N);

        List<UUID> ids = rows.stream().map(LeaderboardRow::getUserId).toList();
        Map<UUID, String> names = users.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<LeaderboardEntry> board = new ArrayList<>();
        int rank = 1;
        for (LeaderboardRow r : rows) {
            board.add(new LeaderboardEntry(
                    rank++,
                    names.getOrDefault(r.getUserId(), "(unknown)"),
                    r.getScore(),
                    r.getDurationSeconds()));
        }
        return board;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}