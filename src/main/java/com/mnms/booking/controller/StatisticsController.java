package com.mnms.booking.controller;

import com.mnms.booking.dto.response.StatisticsQrCodeResponseDTO;
import com.mnms.booking.dto.response.StatisticsUserResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.StatisticsQueryService;
import com.mnms.booking.service.StatisticsUserService;
import com.mnms.booking.service.StatisticsQrCodeService;
import com.mnms.booking.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "통계 API", description = "공연 별 예매자의 정보를 통해 성별/나이, 입장 인원 상황을 확인 가능")
public class StatisticsController {

    private final StatisticsUserService statisticsUserService;
    private final StatisticsQrCodeService statisticsQrCodeService;
    private final StatisticsQueryService statisticsQueryService;

    @GetMapping("/users/{festivalId}")
    @Operation(summary = "feestivalId별 예매자의 성별/나이 통계 조회", description = "특정 페스티벌의 예매자 통계를 조회합니다.")
    public ResponseEntity<SuccessResponse<StatisticsUserResponseDTO>> getFestivalUserStatistics(@PathVariable String festivalId) {
        StatisticsUserResponseDTO statistics = statisticsUserService.getFestivalUserStatistics(festivalId);
        return ApiResponseUtil.success(statistics, "예매자 통계 정보가 성공적으로 조회되었습니다.");
    }

    @GetMapping("/schedules/{festivalId}")
    @Operation(summary = "공연의 모든 유효 공연 날짜/시간 목록 조회",
            description = "주최자가 입장 통계를 조회하기 전, 해당 페스티벌의 유효한(예매 내역이 있는) 모든 공연 날짜와 시간 목록을 가져옵니다.")
    public ResponseEntity<SuccessResponse<List<LocalDateTime>>> getPerformanceDatesForFestival(
            @PathVariable String festivalId,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<LocalDateTime> performanceDates = statisticsQueryService.getPerformanceDatesByFestivalId(festivalId);
        return ApiResponseUtil.success(performanceDates, "공연 날짜 목록이 성공적으로 조회되었습니다.");
    }

    @GetMapping("/enter/{festivalId}")
    @Operation(summary = "공연 날짜별 입장 통계", description = "예매자 및 주최자가 자신의 공연 날짜별 현장 QR 입장 통계를 조회합니다.")
    public ResponseEntity<SuccessResponse<StatisticsQrCodeResponseDTO>> getPerformanceEnterStatistics(
            @PathVariable String festivalId,
            @RequestParam("performanceDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime performanceDate,
            Authentication authentication) {

        String userId = authentication.getName();
        boolean isHost = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HOST"));
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        StatisticsQrCodeResponseDTO statistics = statisticsQrCodeService.getPerformanceEnterStatistics(festivalId, performanceDate, userId, isHost, isAdmin);

        return ApiResponseUtil.success(statistics, "공연 입장 통계 정보가 성공적으로 조회되었습니다.");
    }
}