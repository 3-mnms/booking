package com.mnms.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mnms.booking.dto.request.QrRequestDTO;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final ObjectMapper objectMapper;
    private final QrCodeRepository qrCodeRepository;

    // QrCodeId 생성
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String HEX_CHARS = "0123456789abcdef";

    // QrCodeId : 32자리의 UUID-like 문자열 생성
    public String generateQrCodeId() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            int index = secureRandom.nextInt(HEX_CHARS.length());
            sb.append(HEX_CHARS.charAt(index));
        }
        return sb.toString();
    }

    //
    public BufferedImage generateQrImage(QrRequestDTO payload, int width, int height) throws Exception {
        try {
            String json = objectMapper.writeValueAsString(payload);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(json, BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.QR_PAYLOAD_SERIALIZATION_FAILED);
        } catch (WriterException e) {
            throw new BusinessException(ErrorCode.QR_IMAGE_GENERATION_FAILED);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.QR_IMAGE_CONVERSION_FAILED);
        }
    }

    public byte[] generateQrImageAsBytes(QrRequestDTO payload, int width, int height) throws Exception {
        BufferedImage image = generateQrImage(payload, width, height);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    public String generateQrBase64(QrRequestDTO payload, int width, int height) throws Exception {
        byte[] bytes = generateQrImageAsBytes(payload, width, height);
        return Base64.getEncoder().encodeToString(bytes);
    }
}