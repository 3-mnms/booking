package com.mnms.booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketRequestDTO {
    private Long transfereeId; // userId에 해당함
    private String deliveryMethod;
    private String address;
}