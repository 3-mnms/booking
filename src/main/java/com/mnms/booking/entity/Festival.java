package com.mnms.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;

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
    private LocalDateTime prfpdfrom; // 시작일
    private LocalDateTime prfpdto; // 종료일
    private String poster; // 포스터
    private String ticketPrice; // 가격
    private int availableNOp; // 수용 가능 인원
    private String area; // 장소
    private int paper; // 지류 여부

}