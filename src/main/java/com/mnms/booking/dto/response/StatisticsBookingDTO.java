package com.mnms.booking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "공연 날짜별 예매 현황 및 수용 인원 정보 DTO")
public class StatisticsBookingDTO {

    @Schema(description = "공연 날짜 및 시간", example = "2025-09-01T19:00:00")
    private LocalDateTime performanceDate;

    @Schema(description = "해당 공연의 총 예매자 수", example = "150")
    private int bookingCount;

    @Schema(description = "해당 페스티벌의 총 수용 인원", example = "500")
    private int availableNOP;
}