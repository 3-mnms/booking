package com.mnms.booking.controller;

import com.mnms.booking.dto.request.FestivalRequestDTO;
import com.mnms.booking.dto.request.FestivalSelectDeliveryRequestDTO;
import com.mnms.booking.dto.request.FestivalSelectRequestDTO;
import com.mnms.booking.dto.request.TicketRequestDTO;
import com.mnms.booking.dto.response.FestivalBookingDetailResponseDTO;
import com.mnms.booking.dto.response.FestivalDetailResponseDTO;
import com.mnms.booking.dto.response.UserInfoResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.service.BookingService;
import com.mnms.booking.service.UserService;
import com.mnms.booking.util.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
@Tag(name = "예매 API", description = "예매 티켓 조회, 티켓 선택, 생성")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    /// GET : 페스티벌 예매 정보 조회
    @GetMapping("/detail/phases/1")
    @Operation(summary = "1차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId와 performanceDate(사용자가 선택한 날짜 시간) 으로 공연 상세 정보를 조회합니다." +
                    "selectedTicketCount는 0으로 넣을 것!")
    public FestivalDetailResponseDTO getFestivalDetail(@Valid @RequestBody FestivalSelectRequestDTO request) {
        return bookingService.getFestivalDetail(request);
    }

    /// POST : 페스티벌 예매 정보 1차 선택
    @PostMapping("/selectDate")
    @Operation(summary = "페스티벌 특정 페스티벌 날짜, 시간, 매수 선택",
            description = "festivalId, performanceDate(선택한날짜,시간), selectedTicketCount(매수)를 입력하고 reservationNumber를 반환합니다."
    )
    public String selectFestivalDate(
            @Valid @RequestBody FestivalSelectRequestDTO request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return bookingService.selectFestivalDate(request, principal.userId());
    }

    /// GET : festival 이름, 날짜, 시간, poster url, 매수, 가격, 예매자 이름, 전화번호, email, 티켓 수령 방법, 주소
    @GetMapping("/detail/phases/2")
    @Operation(summary = "2차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId, reservationNumber로 공연 및 예매자 상세 정보를 조회합니다.")
    public FestivalBookingDetailResponseDTO getFestivalBookingDetail(
            @Valid @RequestBody FestivalRequestDTO request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return bookingService.getFestivalBookingDetail(request, principal.userId());
    }

    ///  POST : 페스티벌 예매 정보 2차 선택 - 티켓수령방법
    @PostMapping("/selectDeliveryMethod")
    @Operation(summary = "페스티벌 특정 페스티벌 티켓 수령 방법 선택",
            description = "festivalId, performanceDate(선택한날짜,시간), selectedTicketCount(매수), deliveryMethod(MOBILE or PAPER)"
    )
    public void selectFestivalDelivery(
            @Valid @RequestBody FestivalSelectDeliveryRequestDTO request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        bookingService.selectFestivalDelivery(request, principal.userId());
    }

    /// GET : userId로 예매자 정보 조회
    @GetMapping("/user/info")
    @Operation(summary = "예매자 정보 조회",
            description = "예매 과정에서 예매자 정보를 조회합니다." +
                    "예매자 role이 user인 사람만 조회 가능합니다. (phone, email, address, birth)"
    )
    public ResponseEntity<UserInfoResponseDTO> getUserInfo(@AuthenticationPrincipal JwtPrincipal principal) {
        UserInfoResponseDTO userInfo = userService.getUserInfoById(principal.userId());
        return ResponseEntity.ok(userInfo);
    }

    /// POST : 3차 예매 완료 (결제 직전)
    @PostMapping("/qr")
    @Operation(summary = "페스티벌 예매 티켓 생성",
            description = "사용자가 특정 페스티벌 티켓을 예약하기 위한 마지막 가예매 상태입니다."
    )
    public ResponseEntity<TicketResponseDTO> reserveTicket(
            @Valid @RequestBody FestivalRequestDTO request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        TicketResponseDTO response = bookingService.reserveTicket(request, principal.userId());
        return ResponseEntity.ok(response);
    }

    ///  POST : 가예매 -> 진예매 (결제 완료 확인 상태)

}
