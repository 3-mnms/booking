package com.mnms.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.entity.Ticket;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QrCodeService {

    private final ObjectMapper objectMapper;
    private final QrCodeRepository qrCodeRepository;

    /// QrCodeId 생성
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String HEX_CHARS = "0123456789abcdef";

    /// QrCodeId : 32자리의 UUID-like 문자열 생성
    public String generateQrCodeId() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            int index = secureRandom.nextInt(HEX_CHARS.length());
            sb.append(HEX_CHARS.charAt(index));
        }
        return sb.toString();
    }

    /// QR IMG 조회
    public byte[] generateQrCodeImage(String qrCodeText, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, width, height);

        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        }
    }

    public QrCode getQrCodeByCode(String qrCodeId) {
        return qrCodeRepository.findByQrCodeId(qrCodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_CODE_NOT_FOUND));
    }


    /// 해당 Festival 주최자 QR 스캔
    @Transactional
    public void validateAndUseQrCode(Long userId, String qrCodeId) {

        // QR 코드 조회
        QrCode qrCode = qrCodeRepository.findByQrCodeId(qrCodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_CODE_NOT_FOUND));

        // 주최자 확인
        Ticket ticket = qrCode.getTicket();
        if (!ticket.getFestival().getOrganizer().equals(userId)) {
            throw new BusinessException(ErrorCode.FESTIVAL_MISMATCH);
        }

        // 만료 여부 확인
        if (qrCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.QR_CODE_EXPIRED);
        }

        // 이미 사용된 QR 코드인지 확인
        if (Boolean.TRUE.equals(qrCode.getUsed())) {
            throw new BusinessException(ErrorCode.QR_CODE_ALREADY_USED);
        }

        // QR 코드에 연결된 티켓과 페스티벌 확인
        if (ticket == null || ticket.getFestival() == null) {
            throw new BusinessException(ErrorCode.QR_CODE_INVALID);
        }

        // QR 코드 사용 처리
        qrCode.markAsUsed();
    }
}