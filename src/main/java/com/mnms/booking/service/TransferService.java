package com.mnms.booking.service;

import com.mnms.booking.dto.request.UpdateTicketRequestDTO;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.enums.TicketType;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.QrCodeRepository;
import com.mnms.booking.repository.TicketRepository;
import com.mnms.booking.util.CommonUtils;
import com.mnms.booking.util.UserApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TransferService {

    private final UserApiClient userApiClient;
    private final TicketRepository ticketRepository;
    private final QrCodeRepository qrCodeRepository;
    private final CommonUtils commonUtils;
    private final QrCodeService qrCodeService;

    public void updateTicket(Long ticketId, UpdateTicketRequestDTO request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TICKET_NOT_FOUND));

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
}
