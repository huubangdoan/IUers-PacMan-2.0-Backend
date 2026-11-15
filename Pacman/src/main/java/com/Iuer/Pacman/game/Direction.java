package com.Iuer.Pacman.game;


//  Hướng đi hợp lệ trong giao thức game (chuỗi "up", "down", "left", "right" gửi từ frontend).
public enum Direction {
    UP("up", -1, 0),
    DOWN("down", 1, 0),
    LEFT("left", 0, -1),
    RIGHT("right", 0, 1);

    private final String wireName;
    private final int rowDelta;
    private final int colDelta;

    Direction(String wireName, int rowDelta, int colDelta) {
        this.wireName = wireName;
        this.rowDelta = rowDelta;
        this.colDelta = colDelta;
    }

    public int rowDelta() {
        return rowDelta;
    }

    public int colDelta() {
        return colDelta;
    }

    // Trả về null nếu chuỗi không phải một hướng hợp lệ (giống code gốc: bị từ chối).
    public static Direction fromName(String name) {
        for (Direction direction : values()) {
            if (direction.wireName.equals(name)) return direction;
        }
        return null;
    }
}