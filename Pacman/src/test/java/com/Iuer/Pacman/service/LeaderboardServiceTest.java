package com.Iuer.Pacman.service;

import com.Iuer.Pacman.dto.LeaderboardEntry;
import com.Iuer.Pacman.entity.User;
import com.Iuer.Pacman.repository.GameplayResultRepository;
import com.Iuer.Pacman.repository.GameplayResultRepository.LeaderboardRow;
import com.Iuer.Pacman.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    private static final UUID ID_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID ID_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    @Mock GameplayResultRepository results;
    @Mock UserRepository users;

    private LeaderboardRow row(UUID userId, int score, int duration) {
        LeaderboardRow r = mock(LeaderboardRow.class);
        when(r.getUserId()).thenReturn(userId);
        when(r.getScore()).thenReturn(score);
        when(r.getDurationSeconds()).thenReturn(duration);
        return r;
    }

    private User user(UUID id, String name) {
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(u.getUsername()).thenReturn(name);
        return u;
    }

    @Test
    void assignsRanksAndUsernamesInOrder() {
        LeaderboardRow first = row(ID_B, 7000, 130);
        LeaderboardRow second = row(ID_A, 7000, 150);
        User userA = user(ID_A, "a");
        User userB = user(ID_B, "b");

        when(results.findTopPlayers(null, null, null, 10)).thenReturn(List.of(first, second));
        when(users.findAllById(any())).thenReturn(List.of(userA, userB));

        List<LeaderboardEntry> board = new LeaderboardService(results, users).top10(null, null, null);

        assertEquals(2, board.size());
        assertEquals(new LeaderboardEntry(1, "b", 7000, 130), board.get(0));
        assertEquals(new LeaderboardEntry(2, "a", 7000, 150), board.get(1));
    }

    @Test
    void blankFiltersBecomeNull() {
        when(results.findTopPlayers(null, null, null, 10)).thenReturn(List.of());
        when(users.findAllById(any())).thenReturn(List.of());

        assertEquals(0, new LeaderboardService(results, users).top10(" ", "", null).size());
    }

    @Test
    void unknownUserShowsPlaceholder() {
        LeaderboardRow only = row(ID_A, 100, 60);

        when(results.findTopPlayers(null, null, null, 10)).thenReturn(List.of(only));
        when(users.findAllById(any())).thenReturn(List.of());

        List<LeaderboardEntry> board = new LeaderboardService(results, users).top10(null, null, null);

        assertEquals("(unknown)", board.get(0).username());
    }
}