package com.mnms.booking.service;

import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// MessageListener 인터페이스 구현
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송
    private final ObjectMapper objectMapper; // JSON 파싱을 위한 ObjectMapper

    // Redis로부터 메시지를 수신할 때 호출되는 메서드
    public void onMessage(String message, String channel) {
        // 채널 이름
        log.info("channel name : {}", channel);

        // message는 JSON 문자열 -> DTO로 변환
        try {
            log.info("Received message from Redis channel: {}", message);
            WaitingNumberResponseDTO dto = new ObjectMapper().readValue(message, WaitingNumberResponseDTO.class);

            // userId 기준으로 메시지 전송
            messagingTemplate.convertAndSendToUser(dto.getUserId(), "/queue/waitingNumber", dto);
            log.info("Sent WebSocket message to user {}: {}", dto.getUserId(), dto.getWaitingNumber());

        } catch (Exception e) {
            log.error("예외 발생: {}", e.getMessage(), e);
        }
    }
}