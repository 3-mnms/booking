package com.mnms.booking.dto.request;

import com.mnms.booking.entity.TicketType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketRequestDTO {

    private Long festivalId;
    private int selectedTicketCount; // <= Festival.maxTicketsPerUser
    private LocalDateTime performanceDate;
    private TicketType deliveryMethod;

}