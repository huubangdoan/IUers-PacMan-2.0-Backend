package com.Iuer.Pacman.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(15);

    private final Map<String, Deque<Long>> attemptsCache = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Deque<Long> attempts = attemptsCache.get(key);
        if (attempts == null) {
            return false;
        }
        synchronized (attempts) {
            cleanWindow(attempts, System.currentTimeMillis());
            return attempts.size() >= MAX_ATTEMPTS;
        }
    }

    public void loginFailed(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> attempts = attemptsCache.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (attempts) {
            cleanWindow(attempts, now);
            attempts.addLast(now);
        }
    }

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    private void cleanWindow(Deque<Long> attempts, long now) {
        long windowMillis = WINDOW_DURATION.toMillis();
        while (!attempts.isEmpty() && now - attempts.peekFirst() >= windowMillis) {
            attempts.pollFirst();
        }
    }
}