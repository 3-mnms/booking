package com.mnms.kafka.booking.dto;

import com.mnms.booking.dto.response.ScheduleResponseDTO;
import com.mnms.booking.enums.EventType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FestivalEventDTO {
    private String festivalId;
    private String fname;
    private LocalDate fdfrom;
    private LocalDate fdto;
    private String posterFile;
    private String fcltynm;
    private int ticketPick;
    private int maxPurchase;
    private int ticketPrice;
    private int availableNOP;
    private EventType eventType;
    private Long organizer;

    private List<ScheduleEventDTO> schedules; // 추가
}