package com.Iuer.Pacman.game;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Trạng thái một người chơi trong một ván (tách từ lớp PlayerSession nằm trong GameWebSocketHandler). 
public final class PlayerState {
    private final String username;
    private final GameMapCatalog.GameMode mode;
    private final GameMapCatalog.GameMap map;
    private final Set<String> collected = ConcurrentHashMap.newKeySet();
    private int row;
    private int col;
    private int sequence;
    private int score;
    private boolean finished;

    public PlayerState(String username, GameMapCatalog.GameMode mode, GameMapCatalog.GameMap map) {
        this.username = username;
        this.mode = mode;
        this.map = map;
        this.row = map.playerRow();
        this.col = map.playerCol();
    }

    public String username() { return username; }
    public GameMapCatalog.GameMode mode() { return mode; }
    public GameMapCatalog.GameMap map() { return map; }
    public Set<String> collected() { return collected; }
    public int row() { return row; }
    public int col() { return col; }
    public int sequence() { return sequence; }
    public int score() { return score; }
    public boolean isFinished() { return finished; }

    public void markFinished() { this.finished = true; }

    // Số thứ tự chỉ tăng, không bao giờ giảm (kể cả khi bước đi bị từ chối).
    public void advanceSequenceTo(int newSequence) {
        if (newSequence > sequence) sequence = newSequence;
    }

    // Đặt vị trí mới; cộng điểm nếu là ô chưa ăn. Trả về true nếu vừa ăn thêm một dot
    public boolean moveTo(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
        boolean newDot = collected.add(newRow + ":" + newCol);
        if (newDot) score += mode.pointsPerDot();
        return newDot;
    }
}