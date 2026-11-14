package com.Iuer.Pacman.repository;

import com.Iuer.Pacman.entity.GameScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameScoreRepository extends JpaRepository<GameScore, Long> {

    @Query("SELECT COALESCE(MAX(g.score), 0) FROM GameScore g WHERE g.username = :username")
    Long findBestScoreByUsername(@Param("username") String username);
}
