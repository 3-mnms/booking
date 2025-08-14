package com.mnms.booking.dto.request;


import lombok.Data;
import lombok.Getter;
import java.time.LocalDateTime;

@Data
@Getter
public class BookingSelectRequestDTO {
    private String festivalId;
    private LocalDateTime performanceDate;
    private int selectedTicketCount;
}
