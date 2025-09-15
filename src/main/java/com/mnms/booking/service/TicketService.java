package com.mnms.booking.service;

import com.mnms.booking.dto.response.TicketDetailResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    // 예매 리스트 기본 조회
    public List<TicketResponseDTO> getTicketsByUser(Long userId) {

        // status가 CONFIRMED, CANCELED 일 때
        List<ReservationStatus> statuses = List.of(
                ReservationStatus.CONFIRMED,
                ReservationStatus.CANCELED
        );
        List<Ticket> tickets = ticketRepository.findByUserIdAndReservationStatusIn(userId, statuses);

        if(tickets.isEmpty()){
            return Collections.emptyList();
        }
        return tickets.stream()
                .sorted(Comparator.comparing(Ticket::getReservationDate).reversed())
                .map(ticket -> TicketResponseDTO.fromEntity(ticket, ticket.getFestival()))
                .toList();
    }

    // 예매 상세 조회
    public TicketDetailResponseDTO getTicketDetailByUser(String reservationNumber, Long userId, String userName) {
        Ticket ticket = ticketRepository.findByUserIdAndReservationNumber(userId, reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        if (!ticket.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_UNAUTHORIZED_ACCESS);
        }
        return TicketDetailResponseDTO.fromEntity(ticket, ticket.getFestival(), userName);
    }
}
