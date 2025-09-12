package com.mnms.booking.controller;

import com.mnms.booking.dto.response.TicketDetailResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.TicketService;
import com.mnms.booking.specification.TicketSpecification;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticket")
public class TicketController implements TicketSpecification {

    private final TicketService ticketService;
    private final SecurityResponseUtil securityResponseUtil;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<TicketResponseDTO>>> getUserTickets(Authentication authentication) {
        List<TicketResponseDTO> tickets = ticketService.getTicketsByUser(securityResponseUtil.requireUserId(authentication));
        return ApiResponseUtil.success(tickets);
    }

    @GetMapping("/detail")
    public ResponseEntity<SuccessResponse<TicketDetailResponseDTO>> getUserTicketDetail(@RequestParam String reservationNumber,
                                                                                        Authentication authentication) {
        TicketDetailResponseDTO ticket = ticketService.getTicketDetailByUser(reservationNumber, securityResponseUtil.requireUserId(authentication), securityResponseUtil.requireName(authentication));
        return ApiResponseUtil.success(ticket);
    }
}
