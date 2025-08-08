package com.mnms.booking.controller;

import com.mnms.booking.util.JwtPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/me")
public class MeController {

    @GetMapping
    public Map<String, Object> me(@AuthenticationPrincipal JwtPrincipal principal) {
        return Map.of(
            "userId", principal.userId(),
            "loginId", principal.loginId(),
            "roles", principal.roles()
        );
    }
}