package com.mnms.booking.dto.response;

import com.mnms.booking.entity.Festival;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.TicketType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


// 추후에 지울 수 있음. - 가예매 3차에서 반환값으로 사용했었음.
@Data
@Builder
public class BookingResponseDTO {

    private Long id;
    private String reservationNumber;
    private ReservationStatus reservationStatus;
    private TicketType deliveryMethod;
    private LocalDateTime deliveryDate;
    private Long userId;
    private LocalDateTime reservationDate;
    private List<QrResponseDTO> qrCodes;
    private Festival festival;

    public static BookingResponseDTO fromEntity(Ticket ticket) {
        return BookingResponseDTO.builder()
                .id(ticket.getId())
                .reservationNumber(ticket.getReservationNumber())
                .reservationStatus(ticket.getReservationStatus())
                .deliveryMethod(ticket.getDeliveryMethod())
                .deliveryDate(ticket.getDeliveryDate())
                .reservationDate(ticket.getReservationDate())
                .qrCodes(ticket.getQrCodes().stream()
                        .map(QrResponseDTO::fromEntity)
                        .collect(Collectors.toList()))
                .userId(ticket.getUserId())
                .festival(ticket.getFestival())
                .build();
    }
}

