package com.mnms.booking.controller;

import com.mnms.booking.dto.response.CaptchaResponseDTO;
import com.mnms.booking.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/captcha")
public class CaptchaController {

    private final CaptchaService kaptchaService;

    @GetMapping("/image")
    public void getCaptchaImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        kaptchaService.writeCaptchaImage(request.getSession(), response);
    }

    @PostMapping("/verify")
    public ResponseEntity<CaptchaResponseDTO> verifyCaptcha(
            @RequestParam("captcha") String captcha,
            HttpSession session) {

        CaptchaResponseDTO result = kaptchaService.verifyCaptchaResult(captcha, session);
        return result.isSuccess()
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}