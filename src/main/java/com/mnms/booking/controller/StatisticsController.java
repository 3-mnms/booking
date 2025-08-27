package com.mnms.booking.controller;

import com.mnms.booking.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "통계 API", description = "공연 별 예매자의 정보를 통해 성별/나이, 입장 인원 상황을 확인 가능")
public class StatisticsController {

    private final StatisticsService statisticsService;

    // 생성자 주입
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PostMapping("/{festivalId}")
    @Operation(summary = "feestivalId별 예매자의 성별/나이 통계 조회", description = "")
    public ResponseEntity<Map<String, Object>> getFestivalStatistics(@PathVariable String festivalId) {
        try {
            Map<String, Object> statistics = statisticsService.getFestivalStatistics(festivalId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            // 예외 처리 로직 추가
            e.printStackTrace(); // 실제 서비스에서는 로깅으로 대체
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}