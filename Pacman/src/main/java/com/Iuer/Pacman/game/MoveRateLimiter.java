package com.Iuer.Pacman.game;

public class MoveRateLimiter {
    private final double refillPerSecond;
    private final double capacity;
    private double tokens;
    private long lastNanos;

    public MoveRateLimiter(double refillPerSecond, int burst, long startNanos) {
        this.refillPerSecond = refillPerSecond;
        this.capacity = burst;
        this.tokens = burst;
        this.lastNanos = startNanos;
    }
    public synchronized boolean tryConsume(long nowNanos) {
        if (nowNanos > lastNanos) {
            tokens = Math.min(capacity, tokens + (nowNanos - lastNanos) / 1_000_000_000.0 * refillPerSecond);
            lastNanos = nowNanos;
        }
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }
}
