package com.mnms.booking.controller;

import com.mnms.booking.dto.request.TicketRequestDTO;
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
@RequestMapping("/api/tickets")
@Tag(name = "예매 API", description = "예매 티켓 생성")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    ///  GET : 페스티벌 예매 정보 조회
    ///  POST : 페스티벌 예매 정보 선택


    /// GET : userId로 예매자 정보 조회 - phone, email, address, birth
    @GetMapping("/user/info")
    @Operation(summary = "예매자 정보 조회",
            description = "예매 과정에서 예매자 정보를 조회합니다."
    )
    public ResponseEntity<UserInfoResponseDTO> getUserInfo(@AuthenticationPrincipal JwtPrincipal principal) {
        UserInfoResponseDTO userInfo = userService.getUserInfoById(principal.userId());
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/reserve")
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
