package com.Iuer.Pacman.game.map;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class CompactMap {
    public static final String ID = "compact";

    private static final int[][] GRID = {
            {1,1,1,1,1,1,1,1,1},
            {1,0,0,0,1,0,0,0,1},
            {1,0,1,0,0,0,1,0,1},
            {1,0,1,1,0,1,1,0,1},
            {1,0,0,0,0,0,0,0,1},
            {1,0,1,0,1,0,1,0,1},
            {1,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1}
    };

    private CompactMap() {}

    public static GameMap create() {
        Map<String, Map<String, Object>> features = new LinkedHashMap<>();
        features.put("dots", Map.of("points", 10));
        features.put("playerSpeed", Map.of("multiplier", 1.25));
        return new GameMap(ID, "Compact Maze", GRID, 4, 4, 10,
                List.of(
                        new GhostSpawn(1, 1, "blinky"),
                        new GhostSpawn(1, 7, "pinky"),
                        new GhostSpawn(6, 1, "inky"),
                        new GhostSpawn(6, 7, "clyde")),
                features);
    }
}