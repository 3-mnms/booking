package com.mnms.kafka.booking.dto;

import com.mnms.booking.entity.Schedule;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ScheduleEventDTO {

    private String dayOfWeek; // 요일 코드 (MON, TUE...)
    private String time; // 공연 시작 시간 (HH:mm)

    public static ScheduleEventDTO fromEntity(Schedule schedule) {
        return ScheduleEventDTO.builder()
                .dayOfWeek(schedule.getDayOfWeek())
                .time(schedule.getTime())
                .build();
    }
}