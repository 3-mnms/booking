package com.mnms.booking.service;

import com.mnms.booking.dto.request.TicketTransferRequestDTO;
import com.mnms.booking.dto.request.UpdateTicketRequestDTO;
import com.mnms.booking.dto.response.TicketTransferResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.Transfer;
import com.mnms.booking.enums.TicketType;
import com.mnms.booking.enums.TransferStatus;
import com.mnms.booking.enums.TransferType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.repository.TransferRepository;
import com.mnms.booking.util.CommonUtils;
import com.mnms.booking.util.UserApiClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    private final UserApiClient userApiClient;
    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final TransferRepository transferRepository;
    private final CommonUtils commonUtils;
    private final QrCodeService qrCodeService;

    // 가족간 양도
    @Transactional
    public void updateFamilyTicket(Long ticketId, UpdateTicketRequestDTO request) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        if (ticket.isExpired()) {
            throw new BusinessException(ErrorCode.TICKET_EXPIRED);
        }
        if (ticket.isCanceled()) {
            throw new BusinessException(ErrorCode.TICKET_CANCELED);
        }

        TicketType deliveryMethod = TicketType.valueOf(request.getDeliveryMethod());

        List<QrCode> existingQrs = qrCodeRepository.findByTicketId(ticket.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_CODE_NOT_FOUND));

        // ticket 업데이트
        ticket.updateTicketInfo(
                commonUtils.generateReservationNumber(),
                deliveryMethod,
                request.getTransfereeId(),
                LocalDate.now(),
                request.getAddress()
        );

        // qr code 업데이트
        existingQrs.forEach(qr -> {
            qr.setQrCodeId(qrCodeService.generateQrCodeId());
            qr.setUserId(request.getTransfereeId());
            qr.setTicket(ticket);
        });

        ticket.getQrCodes().clear();
        ticket.getQrCodes().addAll(existingQrs);
    }

    @Transactional
    public void requestTransfer(@Valid TicketTransferRequestDTO dto, Long userId) {
        Ticket ticket = ticketRepository.findReservationNumber(dto.getReservationNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        if(!ticket.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.TICKET_USER_NOT_SAME);
        }

        Transfer transfer = Transfer.builder()
                .senderId(userId)
                .senderName(dto.getSenderName())
                .receiverId(dto.getRecipientId())
                .type("OTHERS".equals(dto.getTransferType()) ? TransferType.OTHERS : TransferType.FAMILY)
                .status(TransferStatus.REQUESTED)
                .build();

        transferRepository.save(transfer);
    }

    public List<TicketTransferResponseDTO> watchTransfer(Long userId) {
        List<Transfer> transfers = transferRepository.findByReceiverId(userId);

        if (transfers.isEmpty()) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_EXIST);
        }

        return transfers.stream()
                .map(transfer -> {
                    Ticket ticket = ticketRepository.findByUserId(transfer.getSenderId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
                    Festival festival = ticket.getFestival();
                    return TicketTransferResponseDTO.from(transfer, ticket, festival);
                })
                .toList();
    }
}
