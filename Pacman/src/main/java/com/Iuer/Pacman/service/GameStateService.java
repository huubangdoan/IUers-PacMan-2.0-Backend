package com.Iuer.Pacman.service;

import com.Iuer.Pacman.state.GameStateChange;
import com.Iuer.Pacman.state.GameStateFlags;
import com.Iuer.Pacman.state.GameStateResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@Service
public class GameStateService {
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);
    private static final String KEY_PREFIX = "game:state:";

    private static final DefaultRedisScript<String> READ_AND_REFRESH = new DefaultRedisScript<>("""
            local state = redis.call('GET', KEYS[1])
            if not state then return nil end
            redis.call('EXPIRE', KEYS[1], ARGV[1])
            return state
            """, String.class);

    private static final DefaultRedisScript<Long> APPLY_CHANGE = new DefaultRedisScript<>("""
            local state = redis.call('GET', KEYS[1])
            if not state then return -1 end
            local mask = tonumber(state)
            local flag = tonumber(ARGV[1])
            local bitIsSet = math.floor(mask / flag) % 2 == 1
            if ARGV[2] == '1' and not bitIsSet then
                mask = mask + flag
            elseif ARGV[2] == '0' and bitIsSet then
                mask = mask - flag
            end
            redis.call('SET', KEYS[1], tostring(mask), 'EX', ARGV[3])
            return mask
            """, Long.class);

    private final StringRedisTemplate redis;

    public GameStateService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public GameStateResponse startOrReset(String username) {
        redis.opsForValue().set(key(username), "0", SESSION_TTL);
        return GameStateResponse.fromMask(0);
    }

    public GameStateResponse getState(String username) {
        String state = redis.execute(
                READ_AND_REFRESH,
                List.of(key(username)),
                String.valueOf(SESSION_TTL.toSeconds())
        );
        if (state == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active game session");
        }
        return GameStateResponse.fromMask(Integer.parseInt(state));
    }

    public GameStateResponse applyEvent(String username, String event, String value) {
        GameStateChange change;
        try {
            change = GameStateFlags.fromEvent(event, value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
        Long mask = redis.execute(
                APPLY_CHANGE,
                List.of(key(username)),
                String.valueOf(change.flag()),
                change.enabled() ? "1" : "0",
                String.valueOf(SESSION_TTL.toSeconds())
        );
        if (mask == null || mask < 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active game session");
        }
        return GameStateResponse.fromMask(Math.toIntExact(mask));
    }

    private String key(String username) {
        return KEY_PREFIX + username;
    }
}
