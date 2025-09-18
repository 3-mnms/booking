package com.mnms.booking.service;

import com.mnms.booking.dto.request.TicketTransferRequestDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.dto.response.TicketTransferResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.Transfer;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.enums.TransferStatus;
import com.mnms.booking.enums.TransferType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.repository.TransferRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    private final TicketRepository ticketRepository;
    private final TransferRepository transferRepository;
    private final QrCodeRepository qrCodeRepository;

    ///  양도 가능한 티켓 조회
    public List<TicketResponseDTO> getTicketsByUser(Long userId) {

        List<Ticket> tickets = ticketRepository.findByUserIdAndReservationStatus(userId, ReservationStatus.CONFIRMED);
        if(tickets.isEmpty()){
            return Collections.emptyList();
        }

        return tickets.stream()
                .filter(ticket -> ticket.getPerformanceDate().isAfter(LocalDateTime.now()))
                .filter(ticket -> !transferRepository.existsByTicketId(ticket.getId()))
                .filter(ticket -> !qrCodeRepository.existsByTicket_IdAndUsedTrue(ticket.getId()))
                .map(ticket -> TicketResponseDTO.fromEntity(ticket, ticket.getFestival()))
                .toList();
    }


    ///  양도 요청
    @Transactional
    public void requestTransfer(@Valid TicketTransferRequestDTO dto, Long userId) {
        Ticket ticket = ticketRepository.findByReservationNumber(dto.getReservationNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        if(transferRepository.existsByTicket_Id(ticket.getId())){
            throw new BusinessException(ErrorCode.TRANSFER_ALREADY_EXIST_REQUEST);
        }

        if(!ticket.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.TICKET_USER_NOT_SAME);
        }

        Transfer transfer = Transfer.builder()
                .ticket(ticket)
                .senderId(userId)
                .senderName(dto.getSenderName())
                .receiverId(dto.getRecipientId())
                .transferType("OTHERS".equals(dto.getTransferType()) ? TransferType.OTHERS : TransferType.FAMILY)
                .status(TransferStatus.REQUESTED)
                .build();
        transferRepository.save(transfer);
    }

    ///  양도 요청 조회
    public List<TicketTransferResponseDTO> watchTransfer(Long userId) {
        List<Transfer> transfers = transferRepository.findByReceiverIdWithTicketAndFestival(
                userId, List.of(TransferStatus.COMPLETED, TransferStatus.CANCELED));

        if (transfers.isEmpty()) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_EXIST);
        }

        return transfers.stream()
                .map(transfer -> {
                    Ticket ticket = transfer.getTicket();
                    if(ticket==null) throw new BusinessException(ErrorCode.TICKET_NOT_FOUND);
                    Festival festival = ticket.getFestival();
                    if(festival==null) throw new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND);
                    return TicketTransferResponseDTO.from(transfer, ticket, festival);
                })
                .toList();
    }

    public Boolean checkStatus(Long transferId) {
        return transferRepository.findTransferStatusById(transferId).equals(TransferStatus.COMPLETED);
    }
}
