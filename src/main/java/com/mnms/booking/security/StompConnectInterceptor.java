package com.mnms.booking.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import java.security.Principal;
import java.util.List;

@Component
@Slf4j
public class StompConnectInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 메시지 접근
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getFirstNativeHeader("X-User-Id");
            String userName = accessor.getFirstNativeHeader("X-User-Name");
            String role = accessor.getFirstNativeHeader("X-User-Role");

            log.info("[STOMP CONNECT] X-User-Id={}, X-User-Name={}, X-User-Role={}", userId, userName, role);

            if (userId != null && role != null) {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );

                accessor.setUser(auth);
                log.info("[STOMP CONNECT] Authentication 등록 완료: {}", auth);
            }
        }

        Principal principal = accessor.getUser();
        if (principal != null) {
            Authentication auth = (Authentication) principal;
            log.info("[STOMP MESSAGE] UserId={}, Roles={}", auth.getName(), auth.getAuthorities());
        }

        return message;
    }



}