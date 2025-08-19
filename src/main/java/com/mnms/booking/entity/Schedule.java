package com.mnms.booking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mnms.kafka.booking.dto.ScheduleEventDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId; // 공연 일정 PK

    @Column(name = "dayOfWeek", length = 3)
    private String dayOfWeek; // 요일 코드 (MON, TUE...)

    @Column(name = "time", length = 5)
    private String time; // 공연 시작 시간 (HH:mm)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    @JsonBackReference
    private Festival festival;

    public static Schedule fromDto(
            ScheduleEventDTO schedule, Festival festival){
        return Schedule.builder()
                .dayOfWeek(schedule.getDayOfWeek())
                .time(schedule.getTime())
                .festival(festival)
                .build();
    }
}