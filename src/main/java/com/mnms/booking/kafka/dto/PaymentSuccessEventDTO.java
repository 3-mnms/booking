package com.mnms.booking.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEventDTO {
    private String method; // payment, cancel, transfer
    private String reservationNumber;
    private  boolean success;
}
