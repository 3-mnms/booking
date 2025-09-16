package com.mnms.booking.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompConnectInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("Before STOMP Principal set for message {}", message);
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getFirstNativeHeader("userId");

            // 여기서 STOMP 세션에 userId으로 등록
            if (userId != null) {
                // STOMP 세션에 Principal 세팅
                accessor.setUser(new StompPrincipal(userId));
                log.info("STOMP Principal set for userId: {}", userId);
            }
        }
        return message;
    }
}