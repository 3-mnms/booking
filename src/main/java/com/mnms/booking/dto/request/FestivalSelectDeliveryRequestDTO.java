package com.mnms.booking.dto.request;

import com.mnms.booking.entity.TicketType;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Getter
public class FestivalSelectDeliveryRequestDTO {
    private String festivalId;
    private LocalDateTime performanceDate;
    private int selectedTicketCount;
    private String deliveryMethod;
}
