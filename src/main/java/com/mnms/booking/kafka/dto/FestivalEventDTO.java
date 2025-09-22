package com.mnms.booking.kafka.dto;

import com.mnms.booking.enums.EventType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FestivalEventDTO {

    private String eventType; // e.g. "FESTIVAL_CREATED" / "FESTIVAL_UPDATED" / "FESTIVAL_DELETED"
    private String id;
    private Long userId;

    private String fname;
    private LocalDate fdfrom;
    private LocalDate fdto;
    private String posterFile;
    private String fcltynm;
    private int ticketPick;
    private int maxPurchase;
    private int ticketPrice;
    private int availableNOP;

    private List<com.mnms.booking.kafka.dto.ScheduleEventDTO> schedules; // 추가
}