package com.Iuer.Pacman.game;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


 // Server-authoritative map and mode catalog; keep grid data aligned with the frontend catalog.
 // Bản này thêm: tên map, vị trí xuất phát của ghost, cấu hình feature (để trả về qua GET /api/maps/{id})
 // và kiểm tra dữ liệu map lúc khởi động (ô xuất phát phải là ô đi được).

public final class GameMapCatalog {
    public record Cell(int row, int col) {}
    public record GhostSpawn(int row, int col, String type) {}
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
            "classic", new GameMap("classic", "Classic Maze", CLASSIC_GRID, 10, 10, 10,
                    List.of(
                            new GhostSpawn(1, 9, "blinky"),
                            new GhostSpawn(1, 11, "pinky"),
                            new GhostSpawn(19, 9, "inky"),
                            new GhostSpawn(19, 11, "clyde")),
                    classicFeatures()),
            "compact", new GameMap("compact", "Compact Maze", COMPACT_GRID, 4, 4, 10,
                    List.of(
                            new GhostSpawn(1, 1, "blinky"),
                            new GhostSpawn(1, 7, "pinky"),
                            new GhostSpawn(6, 1, "inky"),
                            new GhostSpawn(6, 7, "clyde")),
                    compactFeatures())
    );
    private static final Map<String, GameMode> MODES = Map.of(
            "classic", new GameMode("classic", "classic", 10),
            "compact", new GameMode("compact", "compact", 10)
    );

    static {
        MAPS.values().forEach(GameMapCatalog::validate);
        MODES.values().forEach(mode -> {
            if (!MAPS.containsKey(mode.mapId())) {
                throw new IllegalStateException("Mode '" + mode.id() + "' refers to unknown map '" + mode.mapId() + "'");
            }
        });
    }

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

    private static Map<String, Map<String, Object>> classicFeatures() {
        Map<String, Map<String, Object>> features = new LinkedHashMap<>();
        features.put("dots", Map.of("points", 10));
        return features;
    }

    private static Map<String, Map<String, Object>> compactFeatures() {
        Map<String, Map<String, Object>> features = classicFeatures();
        features.put("playerSpeed", Map.of("multiplier", 1.25));
        return features;
    }

    // Fail-fast khi khởi động nếu dữ liệu map sai (giống validateMapDefinition ở frontend)
    private static void validate(GameMap map) {
        int[][] grid = map.grid();
        if (grid.length == 0) throw bad(map, "grid rỗng");
        for (int[] row : grid) {
            if (row.length != grid[0].length) throw bad(map, "grid phải là hình chữ nhật");
            for (int tile : row) {
                if (tile != 0 && tile != 1) throw bad(map, "ô grid chỉ được là 0 hoặc 1");
            }
        }
        if (!isFloor(grid, map.playerRow(), map.playerCol())) throw bad(map, "ô xuất phát của Pacman phải là ô đi được");
        for (GhostSpawn ghost : map.ghosts()) {
            if (ghost.type() == null || ghost.type().isBlank()) throw bad(map, "ghost thiếu type");
            if (!isFloor(grid, ghost.row(), ghost.col())) {
                throw bad(map, "ô xuất phát của ghost '" + ghost.type() + "' phải là ô đi được");
            }
        }
    }

    private static boolean isFloor(int[][] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[row].length && grid[row][col] == 0;
    }

    private static IllegalStateException bad(GameMap map, String reason) {
        return new IllegalStateException("Map '" + map.id() + "' không hợp lệ: " + reason);
    }
}