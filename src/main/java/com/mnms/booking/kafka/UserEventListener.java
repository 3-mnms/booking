package com.mnms.booking.kafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventListener {

    // Kafka로부터 받은 메시지를 처리할 DTO : user 정보로 test
    @Data
    static public class UserEventDTO {
        private Long userId;
        private String loginId;
        private String name;
        private String email;
        private UserEventType eventType; // 예: REGISTERED, DELETED 등
        private String accessToken;
    }

    static public enum UserEventType {
        REGISTERED,
        LOGGED_IN,
        LOGGED_OUT,
        UPDATED_PROFILE,
        DELETED
    }

    // user-event topic 구독
    @KafkaListener(topics = "${app.kafka.topic.user-event}", groupId = "booking-group", containerFactory = "kafkaListenerContainerFactory")
    public void listenOrderEvents(UserEventDTO userEventDto) {
        log.info("새로운 주문 이벤트 수신: " + userEventDto);

        // 여기에 주문 처리 관련 비즈니스 로직을 추가 예정
    }
}