package com.mnms.booking.dto.request;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

// 캡차 정보 DTO
@Getter
public class CaptchaRequestDTO {
    private final String code;
    private final LocalDateTime creationTime;

    public CaptchaRequestDTO(String code) {
        this.code = code;
        this.creationTime = LocalDateTime.now();
    }

    // 캡차 만료 여부 확인
    public boolean isExpired(long expirationMillis) {
        Duration duration = Duration.between(creationTime, LocalDateTime.now());
        return duration.toMillis() > expirationMillis;
    }
}