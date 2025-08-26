package com.mnms.booking.controller;

import com.mnms.booking.entity.QrCode;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.QrCodeService;
import com.mnms.booking.util.ApiResponseUtil;
import com.mnms.booking.util.SecurityResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qr")
@Tag(name = "QR API", description = "QR 이미지 조회, 스캔, ")
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final SecurityResponseUtil securityResponseUtil;

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

    ///  Qrcode 이미지 qrCodeId 리스트로 조회
    @GetMapping("/images")
    @Operation(summary = "QR 코드 이미지 다수 조회",
            description = "qrCodeId 리스트로 여러 개의 QR 코드 이미지를 Base64 인코딩된 문자열로 반환합니다.")
    public ResponseEntity<List<String>> getQrCodeImages(@RequestParam List<String> qrCodeIds) {
        List<String> images = new ArrayList<>();
        for (String qrCodeId : qrCodeIds) {
            QrCode qrCode = qrCodeService.getQrCodeByCode(qrCodeId);
            String qrCodeText = qrCode.getQrCodeId();

            try {
                byte[] imageBytes = qrCodeService.generateQrCodeImage(qrCodeText, 250, 250);
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                images.add(base64Image);
            } catch (Exception e) {
                images.add(null);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.ok(images);
    }


    /// 페스티벌 주최자 QR 스캔
    @PostMapping(value = "/validate/{qrCodeId}")
    @Operation(summary = "QR 코드 스캔 및 유효성 검사",
            description = "qrCodeId와 사용자 ID로 QR 코드 유효성 검사 후 QR 사용 처리합니다.")
    public ResponseEntity<SuccessResponse<Void>> validateAndUseQrCode(
            @PathVariable String qrCodeId,
            Authentication authentication) {

        qrCodeService.validateAndUseQrCode(securityResponseUtil.requireUserId(authentication), qrCodeId);
        return ApiResponseUtil.success(null, "QR 스캔 완료");
    }
}