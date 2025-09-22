package com.mnms.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ws")
@RequiredArgsConstructor
public class WebSocketAdminController {

    private final SimpUserRegistry simpUserRegistry;

    // 현재 WebSocket에 연결된 사용자 확인
    @GetMapping("/users")
    public Set<String> connectedUsers() {
        return simpUserRegistry.getUsers().stream()
                .map(SimpUser::getName)
                .collect(Collectors.toSet());
    }
}