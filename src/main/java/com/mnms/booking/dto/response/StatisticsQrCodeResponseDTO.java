package com.mnms.booking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공연 실시간 입장 통계 응답 DTO")
public class StatisticsQrCodeResponseDTO {

    @Schema(description = "페스티벌 ID", example = "PF272573")
    private String festivalId;

    @Schema(description = "공연 날짜 및 시간", example = "2025-09-07T17:00:00")
    private LocalDateTime performanceDate;

    @Schema(description = "총 예매 가능 인원", example = "100")
    private int availableNOP;

    @Schema(description = "입장 완료된 인원", example = "42")
    private int checkedInCount;
}