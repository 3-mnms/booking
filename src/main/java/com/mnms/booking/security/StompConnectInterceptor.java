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
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 추가 검증 목적
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("CONNECT intercepted for user: {}", accessor.getUser() != null ? accessor.getUser().getName() : "anonymous");
        }
        return message;
    }
}