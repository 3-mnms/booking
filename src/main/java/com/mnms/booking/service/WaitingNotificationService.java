package com.mnms.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class WaitingNotificationService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final WaitingQueueRedisService waitingQueueRedisService;
    private final SimpMessagingTemplate messagingTemplate;

    /// 사용자 대기 순번 조회 및 Redis Pub/Sub으로 발행
    public long getAndPublishWaitingNumber(String waitingQueueKey, String notificationChannelKey, String loginId) {
        try {
            long waitingNumber = waitingQueueRedisService.getWaitingNumber(waitingQueueKey, loginId);
            if (waitingNumber == -1) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND_IN_WAITING);
            }
            publishWaitingNumber(loginId, waitingNumber, notificationChannelKey);
            return waitingNumber;
        } catch (RedisConnectionFailureException e) {
            throw new BusinessException(ErrorCode.REDIS_CONNECTION_FAILED);
        } catch (RedisSystemException e) {
            throw new BusinessException(ErrorCode.REDIS_PUBLISH_FAILED);
        }
    }

    /// Redis Pub/Sub 채널로 대기 순번 정보 발행
    private void publishWaitingNumber(String userId, long waitingNumber, String notificationChannelKey) {
        try {
            WaitingNumberResponseDTO waitingNumberDto = new WaitingNumberResponseDTO(userId, waitingNumber, false, null);
            String message = objectMapper.writeValueAsString(waitingNumberDto);

            // message 발행
            stringRedisTemplate.convertAndSend(notificationChannelKey, message);

        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_SERIALIZATION_FAILED);
        } catch (RedisConnectionFailureException e) {
            throw new BusinessException(ErrorCode.REDIS_CONNECTION_FAILED);
        }
    }

    /// 모든 대기열 사용자에게 순번 업데이트 알림
    public void notifyAllWaitingUsers(String waitingQueueKey, String notificationChannelKey) {
        Set<String> allUsersInQueue = waitingQueueRedisService.getAllUsersInQueue(waitingQueueKey);

        if (allUsersInQueue != null) {
            for (String userId : allUsersInQueue) {
                getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, userId);
            }
        }
    }

    public void notifyAffectedWaitingUsers(String waitingQueueKey, String notificationChannelKey, Long removedRank) {
        if (removedRank == null) {
            return;
        }

        // 대기열 퇴장한 사람 뒤에 있는 사용자만 조회
        Set<String> affectedUsers = waitingQueueRedisService.getUsersByRange(waitingQueueKey, removedRank, -1);
        if (affectedUsers != null) {
            for (String userId : affectedUsers) {
                getAndPublishWaitingNumber(waitingQueueKey, notificationChannelKey, userId);
            }
        }
    }
}