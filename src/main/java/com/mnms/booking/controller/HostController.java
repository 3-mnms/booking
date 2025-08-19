package com.mnms.booking.controller;

import com.mnms.booking.dto.request.HostRequestDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.HostService;
import com.mnms.booking.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host")
@Tag(name = "주최자 도메인 데이터 제공 API", description = "주최자 repo에 제공하는 데이터 입니다. front와 관련 없음")
public class HostController {

    private final HostService hostService;

    @GetMapping("/booking/list")
    @Operation(summary = "주최자 도메인에 예매자 리스트 제공",
            description = "주최자가 FestivalId와 PerformanceDate를 제공하면 해당하는 예매자 userId를 리스트로 제공합니다."
    )
    public ResponseEntity<SuccessResponse<List<Long>>> getBookingsByOrganizer(@RequestBody HostRequestDTO hostRequestDTO) {
        return ApiResponseUtil.success(hostService.getBookingsByOrganizer(hostRequestDTO));
    }
}
