package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.TicketType;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
public class TicketResponseDTO {

    private Long id;
    private String reservationNumber;
    private Boolean reservationStatus;
    private TicketType deliveryMethod;
    private LocalDate deliveryDate;
    private Long userId;
    private LocalDate reservationDate;
    private QrResponseDTO qrCode;
    private Festival festival;

    public static TicketResponseDTO fromEntity(Ticket ticket) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .reservationNumber(ticket.getReservationNumber())
                .reservationStatus(ticket.getReservationStatus())
                .deliveryMethod(ticket.getDeliveryMethod())
                .deliveryDate(ticket.getDeliveryDate())
                .reservationDate(ticket.getReservationDate())
                .qrCode(QrResponseDTO.fromEntity(ticket.getQrCode()))
                .userId(ticket.getUserId())
                .festival(ticket.getFestival())
                .build();
    }
}

