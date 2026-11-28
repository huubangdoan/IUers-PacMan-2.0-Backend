package com.Iuer.Pacman.game;

import java.time.Instant;
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
    private final Instant startedAt;
    private final long startNanos;
    private final MoveRateLimiter moveLimiter;
    private final int totalDots;

    public PlayerState(String username, GameMapCatalog.GameMode mode, GameMapCatalog.GameMap map) {
        this(username, mode, map, Instant.now(), System.nanoTime());
    }

    // Có tham số thời gian để test được bằng đồng hồ giả
    public PlayerState(String username, GameMapCatalog.GameMode mode, GameMapCatalog.GameMap map,
                       Instant startedAt, long startNanos) {
        this.username = username;
        this.mode = mode;
        this.map = map;
        this.row = map.playerRow();
        this.col = map.playerCol();
        this.startedAt = startedAt;
        this.startNanos = startNanos;
        this.moveLimiter = new MoveRateLimiter(SpeedRules.allowedMovesPerSecond(map), SpeedRules.BURST, startNanos);
        this.totalDots = countFloorCells(map.grid());
    }

    // Mỗi ô đi được (giá trị 0) có một dot, kể cả ô xuất phát (giống cách frontend vẽ dot)
    private static int countFloorCells(int[][] grid) {
        int count = 0;
        for (int[] line : grid) for (int tile : line) if (tile == 0) count++;
        return count;
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
    public Instant startedAt() { return startedAt; }
    public MoveRateLimiter moveLimiter() { return moveLimiter; }
    public int totalDots() { return totalDots; }

    // Đã ăn hết dot => thắng (frontend không tự quyết định thắng/thua)
    public boolean isCleared() { return collected.size() >= totalDots; }

    // Thời lượng ván theo đồng hồ SERVER, tối thiểu 1 giây
    public int durationSeconds(long nowNanos) {
        return (int) Math.max(1, Math.round((nowNanos - startNanos) / 1_000_000_000.0));
    }

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