package com.mnms.booking.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Service
public class WaitingQueueRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ZSetOperations<String, String> zSetOperations;

    /// Lua 스크립트
    private static final String ENTER_SCRIPT =
            "local current = redis.call('SCARD', KEYS[1]); " +
                    "if current < tonumber(ARGV[1]) then " +
                    "  redis.call('SADD', KEYS[1], ARGV[2]); " +
                    "  return 1; " +
                    "else " +
                    "  return 0; " +
                    "end";

    private final DefaultRedisScript<Long> enterScript;

    {
        enterScript = new DefaultRedisScript<>();
        enterScript.setScriptText(ENTER_SCRIPT);
        enterScript.setResultType(Long.class);
    }

    /**
     * Lua 스크립트로 안전하게 유저 추가 시도
     * @return true = 즉시 입장, false = 대기열 필요
     */
    public boolean tryEnterBooking(String bookingUsersKey, long availableNOP, String userId) {
        Long result = redisTemplate.execute(
                enterScript,
                Collections.singletonList(bookingUsersKey),
                String.valueOf(availableNOP),
                userId
        );
        return result != null && result == 1;
    }
    //



    public WaitingQueueRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOperations = redisTemplate.opsForZSet();
    }

    public void addUserToQueue(String waitingQueueKey, String loginId) {
        long timestamp = System.currentTimeMillis();
        zSetOperations.add(waitingQueueKey, loginId, timestamp);
        redisTemplate.expire(waitingQueueKey, Duration.ofDays(2));
    }

    public boolean removeUserFromQueue(String waitingQueueKey, String userId) {
        Long removedCount = zSetOperations.remove(waitingQueueKey, userId);
        return removedCount != null && removedCount > 0;
    }

    public Set<String> getAllUsersInQueue(String waitingQueueKey) {
        return zSetOperations.range(waitingQueueKey, 0, -1);
    }

    public String getFirstUserInQueue(String waitingQueueKey) {
        Set<String> users = zSetOperations.range(waitingQueueKey, 0, 0);
        return (users != null && !users.isEmpty()) ? users.iterator().next() : null;
    }

    public long getWaitingNumber(String waitingQueueKey, String userId) {
        Long rank = zSetOperations.rank(waitingQueueKey, userId);
        return (rank != null) ? rank + 1 : -1;
    }

    // 현 예매 페이지에 있는 사용자 수
    public long getBookingUserCount(String bookingUsersKey) {
        Long count = redisTemplate.opsForSet().size(bookingUsersKey);
        return (count != null) ? count : 0L;
    }

    public long getWaitingUserCount(String waitingQueueKey) {
        Long count = redisTemplate.opsForZSet().zCard(waitingQueueKey);
        return (count != null) ? count : 0L;
    }



    public void addBookingUser(String bookingUsersKey, String userId) {
        redisTemplate.opsForSet().add(bookingUsersKey, userId);

        // ttl 생성
        redisTemplate.expire(bookingUsersKey, Duration.ofDays(2));
    }

    public boolean removeBookingUser(String bookingUsersKey, String userId) {
        Long removed = redisTemplate.opsForSet().remove(bookingUsersKey, userId);
        return removed != null && removed > 0;
    }

    public void cleanKey(String key){
        redisTemplate.delete(key);
    }
}
