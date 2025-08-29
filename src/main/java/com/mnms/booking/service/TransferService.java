package com.mnms.booking.service;

import com.mnms.booking.dto.request.TicketTransferRequestDTO;
import com.mnms.booking.dto.request.UpdateTicketRequestDTO;
import com.mnms.booking.dto.response.TicketTransferResponseDTO;
import com.mnms.booking.dto.response.TransferOthersResponseDTO;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final TransferRepository transferRepository;
    private final CommonUtils commonUtils;
    private final QrCodeService qrCodeService;


    ///  양도 요청
    @Transactional
    public void requestTransfer(@Valid TicketTransferRequestDTO dto, Long userId) {
        Ticket ticket = ticketRepository.findByReservationNumber(dto.getReservationNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        if(!ticket.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.TICKET_USER_NOT_SAME);
        }

        Transfer transfer = Transfer.builder()
                .ticket(ticket)
                .senderId(userId)
                .senderName(dto.getSenderName())
                .receiverId(dto.getRecipientId())
                .type("OTHERS".equals(dto.getTransferType()) ? TransferType.OTHERS : TransferType.FAMILY)
                .status(TransferStatus.REQUESTED)
                .build();

        transferRepository.save(transfer);
    }

    ///  양도 요청 조회
    public List<TicketTransferResponseDTO> watchTransfer(Long userId) {
        List<Transfer> transfers = transferRepository.findByReceiverId(userId);

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


    ///  양도 수락
    /// 가족 간 양도 수락
    @Transactional
    public void updateFamilyTicket(UpdateTicketRequestDTO request, Long userId) {
        Transfer transfer = getTransferOrThrow(request.getTransferId());

        validateSenderId(request.getSenderId(), transfer.getTicket().getUserId());

        if(!transfer.getType().equals(TransferType.FAMILY)){
            throw new BusinessException(ErrorCode.TRANSFER_NOT_MATCH_TYPE);
        }
        validateReceiver(transfer, userId);

        // CANCELED 처리
        if (request.getTransferStatusEnum() == TransferStatus.CANCELED) {
            transfer.setStatus(TransferStatus.CANCELED);
            return;
        }

        // ticket 점검
        Ticket ticket = getTicketOrThrow(transfer.getTicket().getReservationNumber());
        validateTicketStatus(ticket);

        // transfer 상태 업데이트
        transfer.setStatus(TransferStatus.COMPLETED);

        // ticket 정보 업데이트
        updateTicketInfo(ticket, request, userId);

        // QR 코드 업데이트
        updateQrCodes(ticket, request, userId);
    }

    /// 지인 간 양도 요청 수락
    @Transactional
    public TransferOthersResponseDTO proceedOthersTicket(UpdateTicketRequestDTO request, Long userId) {

        Transfer transfer = getTransferOrThrow(request.getTransferId());
        validateSenderId(request.getSenderId(), transfer.getTicket().getUserId());

        if(!transfer.getType().equals(TransferType.OTHERS)){
            throw new BusinessException(ErrorCode.TRANSFER_NOT_MATCH_TYPE);
        }

        String reservationNumber = transfer.getTicket().getReservationNumber();
        validateReceiver(transfer, userId);

        // CANCELED 처리
        if (request.getTransferStatusEnum() == TransferStatus.CANCELED) {
            transfer.setStatus(TransferStatus.CANCELED);
            return TransferOthersResponseDTO.builder()
                    .reservationNumber(reservationNumber)
                    .senderId(transfer.getSenderId())
                    .receiverId(userId)
                    .build();
        }

        Ticket ticket = getTicketOrThrow(reservationNumber);
        validateTicketStatus(ticket);
        Festival festival = ticket.getFestival();

        // transfer 업데이트
        transfer.setStatus(TransferStatus.APPROVED);

        return TransferOthersResponseDTO.builder()
                .reservationNumber(ticket.getReservationNumber())
                .senderId(transfer.getSenderId())
                .receiverId(userId)
                .selectedTicketCount(ticket.getSelectedTicketCount())
                .ticketPrice(festival.getTicketPrice())
                .fname(festival.getFname())
                .posterFile(festival.getPosterFile())
                .performanceDate(ticket.getPerformanceDate())
                .build();
    }


    /// 결제 KAFKA 구독 메시지 받고 결제 완료 수행
    @Transactional
    public void updateOthersTicket(UpdateTicketRequestDTO request, Long receiverId) {
        Transfer transfer = getTransferOrThrow(request.getTransferId());

        Ticket ticket = getTicketOrThrow(transfer.getTicket().getReservationNumber());
        validateTicketStatus(ticket);

        transfer.setStatus(TransferStatus.COMPLETED);

        updateTicketInfo(ticket, request, receiverId);
        updateQrCodes(ticket, request, receiverId);
    }


    /// UTIL
    private void validateSenderId(Long senderId, Long ticketUserId) {
        if (!senderId.equals(ticketUserId)) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_MATCH_SENDER);
        }
    }

    private void validateReceiver(Transfer transfer, Long userId){
        // 양수자 확인
        if (!userId.equals(transfer.getReceiverId())){
            throw new BusinessException(ErrorCode.TRANSFER_NOT_MATCH_RECEIVER);
        }
    }
    private Transfer getTransferOrThrow(Long transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSFER_NOT_EXIST));
    }

    private Ticket getTicketOrThrow(String reservationNumber) {
        return ticketRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));
    }

    private void validateTicketStatus(Ticket ticket) {
        if (ticket.isExpired()) {
            throw new BusinessException(ErrorCode.TICKET_EXPIRED);
        }
        if (ticket.isCanceled()) {
            throw new BusinessException(ErrorCode.TICKET_CANCELED);
        }
    }

    /// ticket 정보 업데이트
    private void updateTicketInfo(Ticket ticket, UpdateTicketRequestDTO request, Long receiverId) {
        TicketType deliveryMethod = TicketType.valueOf(request.getDeliveryMethod());

        ticket.updateTicketInfo(
                commonUtils.generateReservationNumber(),
                deliveryMethod,
                receiverId,
                LocalDate.now(),
                request.getAddress()
        );
    }

    /// qr 정보 업데이트
    private void updateQrCodes(Ticket ticket, UpdateTicketRequestDTO request,  Long receiverId) {
        List<QrCode> existingQrs = qrCodeRepository.findByTicketId(ticket.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_CODE_NOT_FOUND));

        existingQrs.forEach(qr -> {
            qr.setQrCodeId(qrCodeService.generateQrCodeId());
            qr.setUserId(receiverId);
            qr.setTicket(ticket);
        });

        ticket.getQrCodes().clear();
        ticket.getQrCodes().addAll(existingQrs);
    }
}
