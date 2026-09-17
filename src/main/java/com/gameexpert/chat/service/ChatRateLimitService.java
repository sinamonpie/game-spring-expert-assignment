package com.gameexpert.chat.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRateLimitService {
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>("""
            local current = tonumber(redis.call('GET', KEYS[1]) or '0')
            if current >= tonumber(ARGV[1]) then
                return 0
            end
            local updated = redis.call('INCR', KEYS[1])
            if updated == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[2])
            end
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public boolean allow(Long playerId) {
        String key = "chat:limit:" + playerId;
        // TODO Lv 19: 횟수 확인부터 최초 만료 설정까지 원자적으로 실행합니다.
        Long result = redisTemplate.execute(RATE_LIMIT_SCRIPT, List.of(key), "5", "10");
        return Long.valueOf(1L).equals(result);
    }
}
