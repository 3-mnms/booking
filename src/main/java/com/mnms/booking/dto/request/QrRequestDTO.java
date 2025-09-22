package com.mnms.booking.dto.request;

import com.mnms.booking.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrRequestDTO {
    private Long id;
    private String qrCodeId;
    private String userId;
    private Ticket ticket;
    private LocalDate issuedAt;
    private LocalDateTime expiredAt;
    private String pinCode;
}