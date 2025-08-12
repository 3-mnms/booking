package com.mnms.booking.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WaitingQueueRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ZSetOperations<String, String> zSetOperations;

    public WaitingQueueRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOperations = redisTemplate.opsForZSet();
    }

    public void addUserToQueue(String waitingQueueKey, String loginId) {
        long timestamp = System.currentTimeMillis();
        zSetOperations.add(waitingQueueKey, loginId, timestamp);
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

    public long getBookingUserCount(String bookingUsersKey) {
        Long count = redisTemplate.opsForSet().size(bookingUsersKey);
        return (count != null) ? count : 0L;
    }

    public void addBookingUser(String bookingUsersKey, String userId) {
        redisTemplate.opsForSet().add(bookingUsersKey, userId);
    }

    public boolean removeBookingUser(String bookingUsersKey, String userId) {
        Long removed = redisTemplate.opsForSet().remove(bookingUsersKey, userId);
        return removed != null && removed > 0;
    }
}
