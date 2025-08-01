package com.mnms.booking.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private final DefaultKaptcha captchaProducer;

    public CaptchaController(DefaultKaptcha captchaProducer) {
        this.captchaProducer = captchaProducer;
    }

    @GetMapping("/image")
    public void getCaptchaImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        // 문자 생성
        String captchaText = captchaProducer.createText();
        request.getSession().setAttribute("captcha", captchaText);
        System.out.println("captchaText : " + captchaText);

        // 이미지 생성
        BufferedImage image = captchaProducer.createImage(captchaText);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        out.flush();
        out.close();
    }
}