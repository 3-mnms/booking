package com.mnms.booking.dto.response;

import lombok.Builder;
import lombok.Data;

@Data // Lombok 사용 시
@Builder
public class KaptchaDto {
    private boolean success;
    private String message;
}
