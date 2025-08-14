package com.mnms.booking.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class FestivalBookingDetailResponseDTO {

    private String festivalName;
    private String posterFile;
    private int ticketPrice;

    private LocalDateTime performanceDate;
    private int ticketCount;
}