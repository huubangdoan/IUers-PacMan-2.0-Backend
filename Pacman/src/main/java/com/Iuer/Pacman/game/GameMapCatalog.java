package com.Iuer.Pacman.game;

import java.util.Map;

/** Server-authoritative map and mode catalog; keep grid data aligned with the frontend catalog. */
public final class GameMapCatalog {
    public record GameMap(String id, int[][] grid, int playerRow, int playerCol, int pointsPerDot) {}
    public record GameMode(String id, String mapId, int pointsPerDot) {}

    private static final int[][] CLASSIC_GRID = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0,1},
            {1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1},
            {1,0,1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0,1},
            {1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1},
            {1,0,1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0,1},
            {1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1},
            {1,0,1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };
    private static final int[][] COMPACT_GRID = {
            {1,1,1,1,1,1,1,1,1},
            {1,0,0,0,1,0,0,0,1},
            {1,0,1,0,0,0,1,0,1},
            {1,0,1,1,0,1,1,0,1},
            {1,0,0,0,0,0,0,0,1},
            {1,0,1,0,1,0,1,0,1},
            {1,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1}
    };

    private static final Map<String, GameMap> MAPS = Map.of(
            "classic", new GameMap("classic", CLASSIC_GRID, 10, 10, 10),
            "compact", new GameMap("compact", COMPACT_GRID, 4, 4, 10)
    );
    private static final Map<String, GameMode> MODES = Map.of(
            "classic", new GameMode("classic", "classic", 10),
            "compact", new GameMode("compact", "compact", 10)
    );

    private GameMapCatalog() {}

    public static GameMode findMode(String id) {
        GameMode mode = MODES.get(id);
        if (mode == null) throw new IllegalArgumentException("Unknown game mode");
        return mode;
    }

    public static GameMap findMap(String id) {
        GameMap map = MAPS.get(id);
        if (map == null) throw new IllegalArgumentException("Unknown game map");
        return map;
    }
}
