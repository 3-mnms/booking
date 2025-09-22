package com.mnms.booking.controller;

import com.mnms.booking.dto.request.BookingRequestDTO;
import com.mnms.booking.dto.request.BookingSelectDeliveryRequestDTO;
import com.mnms.booking.dto.request.BookingSelectRequestDTO;
import com.mnms.booking.dto.response.BookingDetailResponseDTO;
import com.mnms.booking.dto.response.BookingUserResponseDTO;
import com.mnms.booking.dto.response.FestivalDetailResponseDTO;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.BookingQueryService;
import com.mnms.booking.service.BookingCommandService;
import com.mnms.booking.specification.BookingSpecification;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import com.mnms.booking.util.UserApiClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
public class BookingController implements BookingSpecification {

    private final BookingCommandService bookingCommandService;
    private final BookingQueryService bookingQueryService;
    private final UserApiClient userApiClient;
    private final SecurityResponseUtil securityResponseUtil;

    /// GET : 페스티벌 예매 정보 조회
    @PostMapping("/detail/phases/1")
    public ResponseEntity<SuccessResponse<FestivalDetailResponseDTO>> getFestivalDetail(@Valid @RequestBody BookingSelectRequestDTO request) {
        return ApiResponseUtil.success(bookingQueryService.getFestivalDetail(request));
    }

    /// POST : 2차 예매 상세 조회
    @PostMapping("/detail/phases/2")
    public ResponseEntity<SuccessResponse<BookingDetailResponseDTO>> getFestivalBookingDetail(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication
    ) {
        Long userId = securityResponseUtil.requireUserId(authentication);
        return ApiResponseUtil.success(bookingQueryService.getFestivalBookingDetail(request, userId));
    }

    /// POST
    @PostMapping("/selectDate")
    public ResponseEntity<SuccessResponse<String>> selectFestivalDate(
            @Valid @RequestBody BookingSelectRequestDTO request,
            Authentication authentication
    ) {
        return ApiResponseUtil.success(bookingCommandService.selectFestivalDate(request, securityResponseUtil.requireUserId(authentication)));
    }

    /// POST : 배송 선택
    @PostMapping("/selectDeliveryMethod")
    public ResponseEntity<SuccessResponse<Void>> selectFestivalDelivery(
            @Valid @RequestBody BookingSelectDeliveryRequestDTO request,
            Authentication authentication
    ) {
        bookingCommandService.selectFestivalDelivery(request, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(null, "예매 티켓 수령 방법 선택 완료");
    }

    /// POST : 3차 예매 완료 (결제 직전)
    @PostMapping("/qr")
    public ResponseEntity<SuccessResponse<Void>> reserveTicket(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication
    ) {
        bookingCommandService.reserveTicket(request, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(null, "예매 티켓 생성 완료");
    }

    ///  Websocket 메시지 누락 방지 api요청
    @GetMapping("reservation/status")
    public ResponseEntity<SuccessResponse<ReservationStatus>> checkStatus(@RequestParam String reservationNumber){
        return ApiResponseUtil.success(bookingCommandService.checkStatus(reservationNumber));
    }

    /// GET : 예매자 정보 조회
    @GetMapping("/user/info")
    @Override
    public ResponseEntity<SuccessResponse<BookingUserResponseDTO>> getUserInfo(Authentication authentication) {
        return ApiResponseUtil.success(userApiClient.getUserInfoById(securityResponseUtil.requireUserId(authentication)));
    }

    /// POST : 이메일 임시 테스트
    @PostMapping("/email/test")
    public ResponseEntity<String> confirmTicket(
            @RequestParam String reservationNumber,
            @RequestParam boolean paymentStatus) {
        try {
            bookingCommandService.confirmTicket(reservationNumber, paymentStatus);
            return ResponseEntity.ok("예매 상태가 성공적으로 업데이트되고 이메일이 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("에러 발생: " + e.getMessage());
        }
    }
}
