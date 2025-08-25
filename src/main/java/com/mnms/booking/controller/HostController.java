package com.mnms.booking.controller;

import com.mnms.booking.dto.request.HostRequestDTO;
import com.mnms.booking.dto.response.HostResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.HostService;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host")
@Tag(name = "주최자 관련 API", description = "주최자 예매자 명단 조회, 주최자 도메인 데이터 제공 API")
public class HostController {

    private final SecurityResponseUtil securityResponseUtil;
    private final HostService hostService;


    @GetMapping("/list")
    @Operation(summary = "주최자 도메인에 예매자 리스트 제공",
            description = "주최자가 FestivalId와 PerformanceDate를 제공하면 해당하는 예매자 userId를 리스트로 제공합니다. front와 관련 없음"
    )
    public ResponseEntity<SuccessResponse<List<Long>>> getBookingsByOrganizer(@RequestBody HostRequestDTO hostRequestDTO) {
        return ApiResponseUtil.success(hostService.getBookingsByOrganizer(hostRequestDTO));
    }

    /// 주최자 측 예매자 조회
    @PostMapping("/booking/list")
    @Operation(summary = "예매자 정보 조회",
            description = "예매자 정보 조회, 주최자 혹은 운영자로 로그인해야합니다.")
    @PreAuthorize("hasAnyRole('HOST')")
    public ResponseEntity<SuccessResponse<List<HostResponseDTO>>> getBookingInfo(Authentication authentication) {
        List<HostResponseDTO> bookings = hostService.getBookingInfoByHost(securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(bookings);
    }
}
