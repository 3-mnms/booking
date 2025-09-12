package com.mnms.booking.controller;

import com.mnms.booking.dto.request.HostRequestDTO;
import com.mnms.booking.dto.response.HostResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.HostService;
import com.mnms.booking.specification.HostSpecification;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host")
public class HostController implements HostSpecification{

    private final SecurityResponseUtil securityResponseUtil;
    private final HostService hostService;

    @PostMapping("/list")
    public ResponseEntity<SuccessResponse<List<Long>>> getBookingsByOrganizer(@RequestBody HostRequestDTO hostRequestDTO) {
        return ApiResponseUtil.success(hostService.getBookingsByOrganizer(hostRequestDTO));
    }

    /// 주최자 측 예매자 조회
    @PostMapping("/booking/list")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<SuccessResponse<List<HostResponseDTO>>> getBookingInfo(@RequestParam String festivalId,
                                                                                 Authentication authentication) {
        List<HostResponseDTO> bookings = hostService.getBookingInfoByHost(festivalId, securityResponseUtil.requireUserId(authentication), securityResponseUtil.requireRole(authentication));
        return ApiResponseUtil.success(bookings);
    }
}
