package com.mnms.booking.kafka.dto;

import com.mnms.booking.entity.Schedule;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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