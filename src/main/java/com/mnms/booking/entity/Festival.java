package com.mnms.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "festival")
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // festival_id
    private String name; // 이름
    private String frame; // frame
    private LocalDate prfpdfrom; // 시작일
    private LocalDate prfpdto; // 종료일
    private String poster; // 포스터
    private int ticketPrice; // 가격
    private int availableNOp; // 수용 가능 인원
    private String area; // 장소
    private int paper; // 지류 여부

}