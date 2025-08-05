package com.mnms.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingNumberDto {
    private String userId;
    private long waitingNumber; // 대기 순번
    // 선택
    private boolean immediateEntry; // 즉시 입장 여부 추가 (또는 다른 상태 필드)
    private String message; // 사용자에게 보여줄 메시지 추가 (선택 사항)
}