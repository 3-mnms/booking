package com.mnms.booking.dto.response;

import com.mnms.booking.enums.ReservationStatus;

public record TicketStatusResponseDTO(String reservationNumber, ReservationStatus status) {}