package com.mnms.booking.controller;

import com.mnms.booking.dto.request.QrRequestDTO;
import com.mnms.booking.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    // 수정 : qrcode 생성하고 이미지 띄우기
/*    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQr(@RequestParam String qrCodeId) throws Exception {
        QrRequestDTO payload = new QrRequestDTO(
                qrCodeId,
                "user1",
                "ticket4",
                LocalDateTime.now(), // issuedAt
                LocalDateTime.now().plusMinutes(30), // expiredAt
                "9241"
        );
        byte[] qrImage = qrCodeService.generateQrImageAsBytes(payload, 300, 300);
        return ResponseEntity.ok().body(qrImage);
    }*/
}