package com.mnms.booking.controller;

import com.mnms.booking.dto.request.TicketRequestDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.service.BookingService;
import com.mnms.booking.util.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/tickets")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve")
    public ResponseEntity<TicketResponseDTO> reserveTicket(@RequestBody TicketRequestDTO request, @AuthenticationPrincipal JwtPrincipal principal){
        TicketResponseDTO response = bookingService.reserveTicket(request, principal.userId());
        return ResponseEntity.ok(response);
    }
}
