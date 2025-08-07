package com.mnms.booking.controller;

import com.mnms.booking.dto.request.TicketRequestDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve")
    public ResponseEntity<TicketResponseDTO> reserveTicket(@RequestBody TicketRequestDTO request) {
        TicketResponseDTO response = bookingService.reserveTicket(request);
        return ResponseEntity.ok(response);
    }

}
