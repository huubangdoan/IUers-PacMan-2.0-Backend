package com.Iuer.Pacman.game.map;

import java.util.List;
import java.util.Map;

public record GameMap(
        String id,
        String name,
        int[][] grid,
        int playerRow,
        int playerCol,
        int pointsPerDot,
        List<GhostSpawn> ghosts,
        Map<String, Map<String, Object>> features
) {
    public Cell playerCell() {
        return new Cell(playerRow, playerCol);
    }
}