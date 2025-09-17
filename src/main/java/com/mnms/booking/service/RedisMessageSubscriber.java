package com.mnms.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송
    private final ObjectMapper objectMapper; // JSON 파싱을 위한 ObjectMapper
    private final SimpUserRegistry simpUserRegistry;


    // 유저별 대기 메시지 큐
    private final Map<String, Queue<WaitingNumberResponseDTO>> pendingMessages = new ConcurrentHashMap<>();


    // Redis로 메시지 수신할 때 호출됨
    public void onMessage(String message, String channel) {
        printConnectedUsers();

        try {
            WaitingNumberResponseDTO dto = objectMapper.readValue(message, WaitingNumberResponseDTO.class);

            // 연결된 유저인지 확인
            if (isUserConnected(dto.getUserId())) {
                messagingTemplate.convertAndSendToUser(dto.getUserId(), "/queue/waitingNumber", dto);
            } else {
                pendingMessages.computeIfAbsent(dto.getUserId(), k -> new ConcurrentLinkedQueue<>()).add(dto);
            }
        } catch (Exception e) {
            log.error("예외 발생: {}", e.getMessage(), e);
        }
    }

    private boolean isUserConnected(String userId) {
        return simpUserRegistry.getUser(userId) != null;
    }

    // 디버깅용 : 추후 삭제
    private void printConnectedUsers() {
        Collection<SimpUser> users = simpUserRegistry.getUsers();
        log.info("Connected users count: {}", users.size());

        for (SimpUser user : users) {
            log.info("User name (Principal.name): {}", user.getName());
            for (SimpSession session : user.getSessions()) {
                log.info("  Session ID: {}", session.getId());
                log.info("  Session Principal: {}", session.getUser().getName());
            }
        }
    }
}