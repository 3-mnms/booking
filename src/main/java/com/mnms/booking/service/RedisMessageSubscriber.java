package com.mnms.booking.service;

import com.mnms.booking.dto.response.WaitingNumberDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// MessageListener 인터페이스 구현
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송
    private final ObjectMapper objectMapper; // JSON 파싱을 위한 ObjectMapper

    // Redis로부터 메시지를 수신할 때 호출되는 메서드
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis에서 받은 메시지 바디를 문자열로 변환
            String receivedMessage = new String(message.getBody());
            log.info("Received message from Redis channel: {}", receivedMessage);

            // 받은 JSON 문자열을 WaitingNumberDto 객체로 변환
            WaitingNumberDto dto = objectMapper.readValue(receivedMessage, WaitingNumberDto.class);

            // WebSocket을 통해 해당 사용자 토픽으로 메시지 전송
            messagingTemplate.convertAndSend("/topic/waiting/" + dto.getUserId(), dto);
            log.info("Sent WebSocket message to user {}: {}", dto.getUserId(), dto.getWaitingNumber());

        } catch (JsonProcessingException e) {
            log.error("Error parsing Redis message to WaitingNumberDto: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing Redis message: {}", e.getMessage());
        }
    }
}