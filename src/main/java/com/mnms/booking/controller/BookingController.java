package com.mnms.booking.controller;

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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
@Tag(name = "예매 API", description = "예매 티켓 생성")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    ///  GET : 페스티벌 예매 정보 조회
    /// 조회 : festival 이름, 선택 날자, 선택 시간, 가격 전체 날짜, poster url, 제한 매수 (festivalId, 선택날짜시간 전달하면 조회 가능)
    @GetMapping("/detail")
    @Operation(summary = "1차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId와 performanceDate(사용자가 선택한 날짜,시간)으로 공연 상세 정보를 조회합니다.")
    public FestivalDetailResponseDTO getFestivalDetail(@Valid @RequestBody FestivalSelectRequestDTO request) {
        return bookingService.getFestivalDetail(request);
    }

    /// 조회 : festival 이름, 날짜, 시간, poster url, 매수, 가격, 예매자 이름, 전화번호, email, 티켓 수령 방법, 주소
    @GetMapping("/booking/detail")
    @Operation(summary = "2차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId, userId, performanceDate로 공연 및 예매자 상세 정보를 조회합니다.")
    public FestivalBookingDetailResponseDTO getFestivalBookingDetail(
            @Valid @RequestBody FestivalSelectRequestDTO request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return bookingService.getFestivalBookingDetail(request, principal.userId());
    }


    /// POST : 페스티벌 예매 정보 선택
    ///  수정 : 매수, 선택날짜, 선택시간
    @PostMapping("/selectDate")
    @Operation(summary = "페스티벌 날짜, 시간 선택",
            description = "사용자가 특정 페스티벌 날짜, 시간을 선택합니다."
    )
    public void selectFestivalDate(
            @Valid @RequestBody FestivalSelectRequestDTO request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        bookingService.selectFestivalDate(request, principal.userId());
    }

    ///  POST : 티켓수령방법
    @PostMapping("/selectDeliveryMethod")
    @Operation(summary = "페스티벌 티켓 수령 방법 선택",
            description = "사용자가 특정 페스티벌 티켓 수령 방법을 선택합니다."
    )
    public void selectFestivalDelivery(
            @Valid @RequestBody FestivalSelectDeliveryRequestDTO request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        bookingService.selectFestivalDelivery(request, principal.userId());
    }

    /// GET : userId로 예매자 정보 조회 - phone, email, address, birth
    @GetMapping("/user/info")
    @Operation(summary = "예매자 정보 조회",
            description = "예매 과정에서 예매자 정보를 조회합니다."
    )
    public ResponseEntity<UserInfoResponseDTO> getUserInfo(@AuthenticationPrincipal JwtPrincipal principal) {
        UserInfoResponseDTO userInfo = userService.getUserInfoById(principal.userId());
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping
    @Operation(summary = "페스티벌 예매 티켓 생성",
            description = "사용자가 특정 페스티벌 티켓 예매를 완료합니다."
    )
    public ResponseEntity<TicketResponseDTO> reserveTicket(
            @Valid @RequestBody TicketRequestDTO request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        TicketResponseDTO response = bookingService.reserveTicket(request, principal.userId());
        return ResponseEntity.ok(response);
    }
}
