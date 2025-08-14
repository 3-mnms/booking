package com.mnms.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingNotificationService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final WaitingQueueRedisService waitingQueueRedisService;

    /**
     * 사용자 대기 순번 조회 및 Redis Pub/Sub으로 발행
     */
    public long getAndPublishWaitingNumber(String waitingQueueKey, String notificationChannelKey, String loginId) {
        long waitingNumber = waitingQueueRedisService.getWaitingNumber(waitingQueueKey, loginId);
        if (waitingNumber != -1) {
            publishWaitingNumber(loginId, waitingNumber, notificationChannelKey);
            return waitingNumber;
        }
        return -1;
    }

    /**
     * Redis Pub/Sub 채널로 대기 순번 정보 발행
     */
    private void publishWaitingNumber(String userId, long waitingNumber, String notificationChannelKey) {
        try {
            WaitingNumberResponseDTO waitingNumberDto = new WaitingNumberResponseDTO(userId, waitingNumber, false, null);
            String message = objectMapper.writeValueAsString(waitingNumberDto);
            stringRedisTemplate.convertAndSend(notificationChannelKey, message);
        } catch (JsonProcessingException e) {
            log.error("Error converting DTO to JSON: {}", e.getMessage());
        }
    }

    /**
     * 모든 대기열 사용자에게 순번 업데이트 알림
     */
    public void notifyAllWaitingUsers(String waitingQueueKey, String notificationChannelKey) {
        Set<String> allUsersInQueue = waitingQueueRedisService.getAllUsersInQueue(waitingQueueKey);

        if (allUsersInQueue != null) {
            for (String userId : allUsersInQueue) {
                getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, userId);
            }
        }
    }
}