package com.mnms.booking.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class FestivalBookingDetailResponseDTO {

    private String festivalName;
    private LocalDate performanceDate;
    private LocalTime performanceTime;
    private String posterFile;
    private int ticketCount;
    private int ticketPrice;
}