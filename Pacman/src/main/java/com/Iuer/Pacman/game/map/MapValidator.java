package com.Iuer.Pacman.game.map;

// Kiểm tra dữ liệu map (giống validateMapDefinition ở frontend). Gọi lúc khởi động để lỗi cấu hình lộ ra ngay
public final class MapValidator {
    private MapValidator() {}

    public static void validate(GameMap map) {
        int[][] grid = map.grid();
        if (grid.length == 0) throw bad(map, "grid rỗng");
        for (int[] row : grid) {
            if (row.length != grid[0].length) throw bad(map, "grid phải là hình chữ nhật");
            for (int tile : row) {
                if (tile != 0 && tile != 1) throw bad(map, "ô grid chỉ được là 0 hoặc 1");
            }
        }
        if (!isFloor(grid, map.playerRow(), map.playerCol())) {
            throw bad(map, "ô xuất phát của Pacman phải là ô đi được");
        }
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