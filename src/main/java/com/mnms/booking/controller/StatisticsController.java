package com.mnms.booking.controller;

import com.mnms.booking.service.StatisticsUserService;
import com.mnms.booking.service.StatisticsQrCodeService;
import com.mnms.booking.util.SecurityResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "통계 API", description = "공연 별 예매자의 정보를 통해 성별/나이, 입장 인원 상황을 확인 가능")
public class StatisticsController {

    private final StatisticsUserService statisticsUserService;
    private final StatisticsQrCodeService statisticsQrCodeService;

    public StatisticsController(StatisticsUserService statisticsUserService, StatisticsQrCodeService statisticsQrCodeService) {
        this.statisticsUserService = statisticsUserService;
        this.statisticsQrCodeService = statisticsQrCodeService;
    }

    @PostMapping("/{festivalId}")
    @Operation(summary = "feestivalId별 예매자의 성별/나이 통계 조회", description = "특정 페스티벌의 예매자 통계를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getFestivalStatistics(@PathVariable String festivalId) {
        Map<String, Object> statistics = statisticsUserService.getFestivalStatistics(festivalId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{festivalId}/performance-date/attendance")
    @Operation(summary = "공연 날짜별 입장 통계", description = "예매자 및 주최자가 자신의 공연 날짜별 입장 통계를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getPerformanceAttendanceStatistics(
            @PathVariable String festivalId,
            @RequestParam("performanceDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime performanceDate,
            Authentication authentication) {

        String userId = authentication.getName();
        boolean isHost = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HOST"));

        Map<String, Object> statistics = statisticsQrCodeService.getPerformanceAttendanceStatistics(festivalId, performanceDate, userId, isHost);
        return ResponseEntity.ok(statistics);
    }
}