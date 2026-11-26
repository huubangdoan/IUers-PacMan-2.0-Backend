package com.Iuer.Pacman.repository;

import com.Iuer.Pacman.entity.GameplayResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.List;

public interface GameplayResultRepository extends JpaRepository<GameplayResult, Long> {

    interface LeaderboardRow {
        UUID getUserId();
        Integer getScore();
        Integer getDurationSeconds();
    }

    @Query(value = """
            SELECT t.user_id          AS "userId",
                   t.score            AS "score",
                   t.duration_seconds AS "durationSeconds"
            FROM (
                SELECT g.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY g.user_id
                           ORDER BY g.score DESC, g.duration_seconds ASC, g.ended_at ASC, g.id ASC
                       ) AS rn
                FROM gameplay_results g
                WHERE g.outcome = 'WIN'
                  AND (CAST(:map AS TEXT)     IS NULL OR g.map_name     = CAST(:map AS TEXT))
                  AND (CAST(:mode AS TEXT)    IS NULL OR g.game_mode    = CAST(:mode AS TEXT))
                  AND (CAST(:version AS TEXT) IS NULL OR g.game_version = CAST(:version AS TEXT))
            ) t
            WHERE t.rn = 1
            ORDER BY t.score DESC, t.duration_seconds ASC, t.ended_at ASC, t.user_id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<LeaderboardRow> findTopPlayers(@Param("map") String map,
                                        @Param("mode") String mode,
                                        @Param("version") String version,
                                        @Param("limit") int limit);
}