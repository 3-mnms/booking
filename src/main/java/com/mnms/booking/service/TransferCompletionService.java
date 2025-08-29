package com.mnms.booking.service;

import com.mnms.booking.dto.request.UpdateTicketRequestDTO;
import com.mnms.booking.dto.response.TicketStatusResponseDTO;
import com.mnms.booking.dto.response.TransferOthersResponseDTO;
import com.mnms.booking.dto.response.TransferStatusResponseDTO;
import com.mnms.booking.entity.Festival;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.entity.Transfer;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.enums.TicketType;
import com.mnms.booking.enums.TransferStatus;
import com.mnms.booking.enums.TransferType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.repository.TransferRepository;
import com.mnms.booking.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

///  양도 수락
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TransferCompletionService {

    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final TransferRepository transferRepository;
    private final CommonUtils commonUtils;
    private final QrCodeService qrCodeService;
    private final SimpMessagingTemplate messagingTemplate;

    /// 가족 간 양도 수락
    @Transactional(rollbackFor = BusinessException.class)
    public void updateFamilyTicket(UpdateTicketRequestDTO request, Long userId) {
        Transfer transfer = getTransferOrThrow(request.getTransferId());

        validateSenderId(request.getSenderId(), transfer.getTicket().getUserId());
        validateTransferType(transfer, TransferType.FAMILY);
        validateReceiver(transfer, userId);

        // CANCELED 처리
        if (handleCancel(transfer, request)) {
            return;
        }

        // transfer 상태 업데이트
        transfer.setStatus(TransferStatus.COMPLETED);
        applyTicketAndQrUpdate(transfer, request, transfer.getReceiverId(), true);
    }

    /// 지인 간 양도 요청 수락
    @Transactional
    public TransferOthersResponseDTO proceedOthersTicket(UpdateTicketRequestDTO request, Long userId) {

        Transfer transfer = getTransferOrThrow(request.getTransferId());
        validateSenderId(request.getSenderId(), transfer.getTicket().getUserId());
        validateTransferType(transfer, TransferType.OTHERS);

        String reservationNumber = transfer.getTicket().getReservationNumber();
        validateReceiver(transfer, userId);

        // CANCELED 처리
        if(handleCancel(transfer, request)){
            return TransferOthersResponseDTO.canceled(reservationNumber, transfer.getSenderId(), userId);
        }

        Ticket ticket = getTicketOrThrow(reservationNumber);
        validateTicketStatus(ticket);
        Festival festival = ticket.getFestival();

        // transfer 업데이트
        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setTicketType(request.getTicketType());
        transfer.setAddress(request.getAddress());

        return TransferOthersResponseDTO.from(transfer, ticket, festival, userId);
    }


    /// 결제 KAFKA 구독 메시지 받고 결제 완료 수행
    @Transactional
    public void updateOthersTicket(String reservationNumber, boolean paymentStatus) {
        Ticket ticket = ticketRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

        Transfer transfer = transferRepository.findByTicket(ticket);

        TransferStatus newStatus = paymentStatus ?
                TransferStatus.COMPLETED :
                TransferStatus.APPROVED;

        // 결제 kafka 로직 변경 시 수정 예정
        if (paymentStatus) {
            transfer.setStatus(TransferStatus.COMPLETED);
            UpdateTicketRequestDTO request = UpdateTicketRequestDTO.builder()
                    .transferId(transfer.getId())
                    .senderId(transfer.getSenderId())
                    .transferStatus(transfer.getStatus())
                    .ticketType(transfer.getTicketType())
                    .address(transfer.getAddress())
                    .build();
            applyTicketAndQrUpdate(transfer, request, transfer.getReceiverId(), false);
        }
        // websocket 전송
        messagingTemplate.convertAndSend(
                "/topic/transfer/" + transfer.getReceiverId(),
                new TransferStatusResponseDTO(ticket.getReservationNumber(), newStatus));
    }

    /// UTIL
    private void applyTicketAndQrUpdate(Transfer transfer, UpdateTicketRequestDTO request,
                                        Long receiverId, boolean updateTransferFields) {
        Ticket ticket = getTicketOrThrow(transfer.getTicket().getReservationNumber());
        validateTicketStatus(ticket);

        if (updateTransferFields) {
            transfer.setTicketType(request.getTicketType());
            transfer.setAddress(request.getAddress());
        }

        updateTicketInfo(ticket, request, receiverId);
        updateQrCodes(ticket, request, receiverId);
    }

    private boolean handleCancel(Transfer transfer, UpdateTicketRequestDTO request) {
        if (request.getTransferStatus() == TransferStatus.CANCELED) {
            transfer.setStatus(TransferStatus.CANCELED);
            return true;
        }
        return false;
    }

    private void validateTransferType(Transfer transfer, TransferType expectedType) {
        if (!transfer.getTransferType().equals(expectedType)) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_MATCH_TYPE);
        }
    }

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
        TicketType deliveryMethod = request.getTicketType();

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
    }
}
