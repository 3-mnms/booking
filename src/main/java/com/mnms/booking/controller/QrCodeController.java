package com.mnms.booking.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mnms.booking.dto.request.QrRequestDTO;
import com.mnms.booking.entity.QrCode;
import com.mnms.booking.service.QrCodeService;
import com.mnms.booking.util.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qr")
@Tag(name = "QR API", description = "QR 이미지 조회, 스캔, ")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    /// Qrcode 이미지 조회
    @GetMapping(value = "/image/{qrCodeId}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "QR 코드 이미지 조회",
            description = "qrCodeId로 QR 코드 이미지를 PNG 형식으로 반환합니다.")
    public ResponseEntity<byte[]> getQrCodeImage(@PathVariable String qrCodeId) {
        QrCode qrCode = qrCodeService.getQrCodeByCode(qrCodeId);
        String qrCodeText = qrCode.getQrCodeId();

        try {
            byte[] imageBytes = qrCodeService.generateQrCodeImage(qrCodeText, 250, 250);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /// 페스티벌 주최자 QR 스캔
    @PostMapping(value = "/validate/{qrCodeId}")
    @Operation(summary = "QR 코드 스캔 및 유효성 검사",
            description = "qrCodeId와 사용자 ID로 QR 코드 유효성 검사 후 QR 사용 처리합니다.")
    public ResponseEntity<Void> validateAndUseQrCode(
            @PathVariable String qrCodeId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        qrCodeService.validateAndUseQrCode(principal.userId(), qrCodeId);
        return ResponseEntity.ok().build();
    }
}