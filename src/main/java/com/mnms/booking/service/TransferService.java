package com.mnms.booking.service;

import com.mnms.booking.dto.request.TicketTransferRequestDTO;
import com.mnms.booking.dto.response.TicketTransferResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.Transfer;
import com.mnms.booking.enums.TransferStatus;
import com.mnms.booking.enums.TransferType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.repository.TransferRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    private final TicketRepository ticketRepository;
    private final TransferRepository transferRepository;

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

        transfers.forEach(transfer -> log.info("transfer: {}", transfer));
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
}
