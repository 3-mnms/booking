package com.mnms.booking.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FestivalRequestDTO {

    private String id; // 공연 고유 ID (PF000001)

    private String fname; // 공연명

    private LocalDate fdfrom; // 공연 시작일

    private LocalDate fdto; // 공연 종료일

    private String posterFile; // 공연 대표 이미지 URL

    private String fcltynm; // 공연장 장소

    private int ticketPick; // 티켓 배송 방법

    private int maxPurchase; // 1회 최대 구매 가능 수량

    private int ticketPrice; // 티켓 가격

    private int availableNOP; // 수용인원

    private List<ScheduleDTO> schedules;

    @Data
    public static class ScheduleDTO {
        private Long scheduleId;
        private String dayOfWeek;
        private String time;
    }
}