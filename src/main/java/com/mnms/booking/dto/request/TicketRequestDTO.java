package com.mnms.booking.dto.request;

import com.mnms.booking.entity.TicketType;
import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TicketRequestDTO {

    private Long festivalId;
    private int selectedTicketCount; // <= Festival.maxTicketsPerUser
    private LocalDate performanceDate;
    private LocalTime performanceTime;
    private TicketType deliveryMethod;

}