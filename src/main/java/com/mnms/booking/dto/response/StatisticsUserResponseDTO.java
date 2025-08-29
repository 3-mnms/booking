package com.mnms.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공연 예매자 통계 응답 DTO")
public class StatisticsUserResponseDTO {

    @Schema(description = "총 예매자 수", example = "50")
    private int totalPopulation;

    @Schema(description = "성별 예매자 수", example = "{\"male\": 25, \"female\": 25}")
    private Map<String, Integer> genderCount;

    @Schema(description = "성별 비율(%)", example = "{\"male\": \"50.00%\", \"female\": \"50.00%\"}")
    private Map<String, String> genderPercentage;

    @Schema(description = "연령대별 예매자 수", example = "{\"10대\": 5, \"20대\": 20, \"30대\": 15, \"40대\": 8, \"50대 이상\": 2}")
    private Map<String, Integer> ageGroupCount;

    @Schema(description = "연령대별 비율(%)", example = "{\"10대\": \"10.00%\", \"20대\": \"40.00%\", \"30대\": \"30.00%\", \"40대\": \"16.00%\", \"50대 이상\": \"4.00%\"}")
    private Map<String, String> ageGroupPercentage;
}