package com.mnms.booking.dto.request;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class BookingSelectDeliveryRequestDTO {
    private String festivalId;
    private String reservationNumber;
    private String deliveryMethod;
    private String address;
}
