package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ScheduleResponseDTO {
    private String dayOfWeek; // 요일 코드
    private String time;      // 공연 시작 시간 (HH:mm)
}
