package com.mnms.booking.service;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.mnms.booking.dto.request.CaptchaRequestDTO;
import com.mnms.booking.dto.response.CaptchaResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class CaptchaService {

    private final DefaultKaptcha captchaProducer;
    private static final String CAPTCHA_SESSION_KEY = "captchaCode";
    private static final long CAPTCHA_EXPIRATION_MILLIS = 3 * 60 * 1000L;


    // 캡차 이미지 생성
    public void writeCaptchaImage(HttpSession session, HttpServletResponse response) throws IOException {
        // 보안 설정
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        BufferedImage image = generateCaptchaImage(session);

        try (OutputStream out = response.getOutputStream()) {
            ImageIO.write(image, "jpg", out);
            out.flush();
        }
    }

    private BufferedImage generateCaptchaImage(HttpSession session) {
        String captchaText = captchaProducer.createText();
        session.setAttribute(CAPTCHA_SESSION_KEY, new CaptchaRequestDTO(captchaText));
        log.info("Generated Kaptcha Text: {}", captchaText);
        return captchaProducer.createImage(captchaText);
    }

    public CaptchaResponseDTO verifyCaptchaResult(String userInputCaptcha, HttpSession session) {
        CaptchaRequestDTO captchaRequest = (CaptchaRequestDTO) session.getAttribute(CAPTCHA_SESSION_KEY);

        if (captchaRequest == null || captchaRequest.isExpired(CAPTCHA_EXPIRATION_MILLIS)) { // 3분 만료
            session.removeAttribute(CAPTCHA_SESSION_KEY);
            return CaptchaResponseDTO.builder()
                    .success(false)
                    .message("보안문자가 만료되었습니다.")
                    .build();
        }

        boolean isValid = captchaRequest.getCode().equalsIgnoreCase(userInputCaptcha);
        if (isValid) {session.removeAttribute(CAPTCHA_SESSION_KEY);}

        return CaptchaResponseDTO.builder()
                .success(isValid)
                .message(isValid ? "인증 성공" : "보안문자 불일치")
                .build();
    }
}