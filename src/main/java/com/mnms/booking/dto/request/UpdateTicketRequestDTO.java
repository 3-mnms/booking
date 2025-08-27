package com.mnms.booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketRequestDTO {
    private Long transferId;
    private Long receiverId;
    private String transferStatus;

    private String deliveryMethod;
    private String address;
}