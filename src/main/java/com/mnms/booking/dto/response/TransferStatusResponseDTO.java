package com.mnms.booking.dto.response;

import com.mnms.booking.enums.TransferStatus;

public record TransferStatusResponseDTO(String reservationNumber, TransferStatus status) {}