package com.mnms.booking.controller;

import com.mnms.booking.dto.response.StatisticsBookingDTO;
import com.mnms.booking.dto.response.StatisticsQrCodeResponseDTO;
import com.mnms.booking.dto.response.StatisticsUserResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.StatisticsQueryService;
import com.mnms.booking.service.StatisticsUserService;
import com.mnms.booking.service.StatisticsQrCodeService;
import com.mnms.booking.specification.StatisticsSpecification;
import com.mnms.booking.util.ApiResponseUtil;
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
public class StatisticsController implements StatisticsSpecification {

    private final StatisticsUserService statisticsUserService;
    private final StatisticsQrCodeService statisticsQrCodeService;
    private final StatisticsQueryService statisticsQueryService;

    @GetMapping("/users/{festivalId}")
    public ResponseEntity<SuccessResponse<StatisticsUserResponseDTO>> getFestivalUserStatistics(@PathVariable String festivalId) {
        StatisticsUserResponseDTO statistics = statisticsUserService.getFestivalUserStatistics(festivalId);
        return ApiResponseUtil.success(statistics, "예매자 통계 정보가 성공적으로 조회되었습니다.");
    }


    @GetMapping("/schedules/{festivalId}")
    public ResponseEntity<SuccessResponse<List<LocalDateTime>>> getPerformanceDatesForFestival(
            @PathVariable String festivalId,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<LocalDateTime> performanceDates = statisticsQueryService.getPerformanceDatesByFestivalId(festivalId);
        return ApiResponseUtil.success(performanceDates, "공연 날짜 목록이 성공적으로 조회되었습니다.");
    }


    @GetMapping("/enter/{festivalId}")
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

    @GetMapping("/booking/{festivalId}")
    public ResponseEntity<SuccessResponse<List<StatisticsBookingDTO>>> getBookingSummary(
            @PathVariable String festivalId,
            Authentication authentication) {

        String userId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        statisticsQueryService.validateHostOrAdminAccess(festivalId, userId, isAdmin);

        List<StatisticsBookingDTO> summary = statisticsQueryService.getBookingSummary(festivalId);

        return ApiResponseUtil.success(summary, "공연 요약 정보가 성공적으로 조회되었습니다.");
    }
}