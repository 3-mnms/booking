package com.mnms.booking.controller;

import com.mnms.booking.dto.response.TicketDetailResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.TicketService;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticket")
@Tag(name = "예매 내역 API", description = "예매 내역 조회")
public class TicketController {

    private final TicketService ticketService;
    private final SecurityResponseUtil securityResponseUtil;

    @GetMapping("/user")
    @Operation(summary = "예매 정보 정보 조회",
            description = "예매 완료한 티켓 정보를 조회" +
                    "예매자가 예매 완료한 티켓 정보를 조회할 수 있다."
    )
    public ResponseEntity<SuccessResponse<List<TicketResponseDTO>>> getUserTickets(Authentication authentication) {
        List<TicketResponseDTO> tickets = ticketService.getTicketsByUser(securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(tickets);
    }

    @GetMapping("/user/detail")
    @Operation(summary = "예매자 정보 조회",
            description = "예매 완료한 티켓 정보를 조회" +
                    "예매자가 예매 완료한 티켓 정보를 조회할 수 있다."
    )
    public ResponseEntity<SuccessResponse<TicketDetailResponseDTO>> getUserTicketDetail(@Valid @RequestBody String reservationNumber,
                                                                                        Authentication authentication) {
        TicketDetailResponseDTO ticket = ticketService.getTicketDetailByUser(reservationNumber, securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(ticket);
    }

}
