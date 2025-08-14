package com.mnms.booking.dto.request;

import lombok.Data;
import lombok.Getter;

@Data @Getter
public class BookingRequestDTO {
    private String festivalId;
    private String reservationNumber;
}
