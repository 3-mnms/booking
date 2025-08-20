package com.mnms.booking.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEventDTO {
    private String reservationNumber;
    private Boolean paymentStatus;
}
