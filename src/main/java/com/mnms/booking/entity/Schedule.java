package com.mnms.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
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
    private Festival festival;
}