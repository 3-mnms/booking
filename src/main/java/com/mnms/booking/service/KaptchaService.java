package com.mnms.booking.service;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.mnms.booking.dto.response.KaptchaDTO;
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
public class KaptchaService {

    private final DefaultKaptcha captchaProducer;
    private static final String CAPTCHA_SESSION_KEY = "captchaCode";

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
        session.setAttribute(CAPTCHA_SESSION_KEY, captchaText);
        log.info("Generated Kaptcha Text: {}", captchaText);

        return captchaProducer.createImage(captchaText);
    }

    public KaptchaDTO verifyCaptchaResult(String userInputCaptcha, HttpSession session) {
        String sessionCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);

        boolean isValid = sessionCaptcha != null && sessionCaptcha.equalsIgnoreCase(userInputCaptcha);
        if (isValid) {session.removeAttribute(CAPTCHA_SESSION_KEY);}

        return KaptchaDTO.builder()
                .success(isValid)
                .message(isValid ? "인증 성공" : "보안문자 불일치")
                .build();
    }
}