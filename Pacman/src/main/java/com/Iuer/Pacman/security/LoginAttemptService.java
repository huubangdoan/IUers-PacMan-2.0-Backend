package com.Iuer.Pacman.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

import java.time.Duration;
@Component 
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;

    private static final Duration WINDOW_DURATION = Duration.ofMinutes(15);

    private final Map<String, Deque<Long>> attemptsCache = new ConcurrentHashMap<>();

    
    public boolean isBlocked(String key){
        cleanWindow(key);
        Deque<Long> attempts = attemptsCache.get(key);
        return attempts != null && attempts.size() >= MAX_ATTEMPTS;
    }
    private void cleanWindow(Deque<Long> attempts, long now) {

        long windowMillis = WINDOW_DURATION.toMillis();

        while (!attempts.isEmpty()
                && now - attempts.peekFirst() >= windowMillis) {

            attempts.pollFirst();
        }
    }

    public void loginFailed(String key){
        long now = System.currentTimeMillis();
        attemptsCache.putIfAbsent(key, new ArrayDeque<>());
        Deque<Long> attempts = attemptsCache.get(key);

        synchronized (attempts){
            cleanWindow(key);
            attempts.addLast(now);
        }

    }
}
