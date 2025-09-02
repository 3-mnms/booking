package com.mnms.booking.controller;

import com.mnms.booking.dto.request.BookingRequestDTO;
import com.mnms.booking.dto.request.BookingSelectDeliveryRequestDTO;
import com.mnms.booking.dto.request.BookingSelectRequestDTO;
import com.mnms.booking.dto.response.BookingDetailResponseDTO;
import com.mnms.booking.dto.response.FestivalDetailResponseDTO;
import com.mnms.booking.dto.response.UserInfoResponseDTO;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.BookingQueryService;
import com.mnms.booking.service.BookingCommandService;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import com.mnms.booking.util.UserApiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
@Tag(name = "예매 API", description = "예매 티켓 조회, 티켓 선택, 생성")
public class BookingController {

    private final BookingCommandService bookingCommandService;
    private final BookingQueryService bookingQueryService;
    private final UserApiClient userApiClient;
    private final SecurityResponseUtil securityResponseUtil;

    /// GET : 페스티벌 예매 정보 조회
    @PostMapping("/detail/phases/1")
    @Operation(summary = "1차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId와 performanceDate(사용자가 선택한 날짜, 시간)으로 공연 상세 정보를 조회합니다." +
                    "selectedTicketCount는 0으로 넣을 것!")
    public ResponseEntity<SuccessResponse<FestivalDetailResponseDTO>> getFestivalDetail(@Valid @RequestBody BookingSelectRequestDTO request) {
        return ApiResponseUtil.success(bookingQueryService.getFestivalDetail(request));
    }

    @PostMapping("/detail/phases/2")
    @Operation(summary = "2차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId, reservationNumber로 공연 및 예매자 상세 정보를 조회합니다.")
    public ResponseEntity<SuccessResponse<BookingDetailResponseDTO>> getFestivalBookingDetail(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication
    ) {
        Long userId = securityResponseUtil.requireUserId(authentication);
        return ApiResponseUtil.success(bookingQueryService.getFestivalBookingDetail(request, userId));
    }

    /// POST
    @PostMapping("/selectDate")
    @Operation(summary = "페스티벌 특정 페스티벌 날짜, 시간, 매수 선택",
            description = "festivalId, performanceDate(선택한날짜,시간), selectedTicketCount(매수)를 입력하고 reservationNumber를 반환합니다."
    )
    public ResponseEntity<SuccessResponse<String>> selectFestivalDate(
            @Valid @RequestBody BookingSelectRequestDTO request,
            Authentication authentication
    ) {
        return ApiResponseUtil.success(bookingCommandService.selectFestivalDate(request, securityResponseUtil.requireUserId(authentication)));
    }

    @PostMapping("/selectDeliveryMethod")
    @Operation(summary = "페스티벌 특정 페스티벌 티켓 수령 방법, 주소 선택",
            description = "festivalId, performanceDate(선택한날짜,시간), deliveryMethod(MOBILE or PAPER), address(String)"
    )
    public ResponseEntity<SuccessResponse<Void>> selectFestivalDelivery(
            @Valid @RequestBody BookingSelectDeliveryRequestDTO request,
            Authentication authentication
    ) {
        bookingCommandService.selectFestivalDelivery(request, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(null, "예매 티켓 수령 방법 선택 완료");
    }

    /// POST : 3차 예매 완료 (결제 직전)
    @PostMapping("/qr")
    @Operation(summary = "페스티벌 예매 티켓 생성",
            description = "사용자가 특정 페스티벌 티켓을 예약하기 위한 마지막 가예매 상태입니다."
    )
    public ResponseEntity<SuccessResponse<Void>> reserveTicket(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication
    ) {
        bookingCommandService.reserveTicket(request, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(null, "예매 티켓 생성 완료");
    }

    ///  Websocket 메시지 누락 방지 api요청
    @GetMapping("reservation/status")
    @Operation( summary = "예매 완료/취소 정보 조회",
            description = "Websocket 메시지 누락 시, " +
                    "예매 완료 혹은 취소 되었는지 확인합니다. ")
    public ResponseEntity<SuccessResponse<ReservationStatus>> checkStatus(@RequestParam String reservationNumber){
        return ApiResponseUtil.success(bookingCommandService.checkStatus(reservationNumber));
    }

    ///  GET
    @GetMapping("/user/info")
    @Operation(summary = "예매자 정보 조회",
            description = "예매 과정에서 예매자 정보를 조회합니다." +
                    "예매자 role이 user인 사람만 조회 가능합니다. (phone, email, address, birth)"
    )
    public ResponseEntity<SuccessResponse<UserInfoResponseDTO>> getUserInfo(Authentication authentication) {
        return ApiResponseUtil.success(userApiClient.getUserInfoById(securityResponseUtil.requireUserId(authentication)));
    }
}
