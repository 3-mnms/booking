package com.mnms.booking.dto.request;

import com.mnms.booking.entity.TicketType;
import lombok.Data;

@Data
public class TicketRequestDTO {

    //private Long id;
    //private String userId;
    private Long festivalId;
    private TicketType deliveryMethod;

}