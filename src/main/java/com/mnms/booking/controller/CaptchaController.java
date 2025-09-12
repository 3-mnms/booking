package com.mnms.booking.controller;

import com.mnms.booking.dto.response.CaptchaResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.CaptchaService;
import com.mnms.booking.specification.CaptchaSpecification;
import com.mnms.booking.util.ApiResponseUtil;
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
public class CaptchaController implements CaptchaSpecification{

    private final CaptchaService kaptchaService;

    @GetMapping("/image")
    public ResponseEntity<SuccessResponse<Void>> getCaptchaImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        kaptchaService.writeCaptchaImage(request.getSession(), response);
        return ApiResponseUtil.success(null, "보안문자 이미지가 생성 완료");
    }

    @PostMapping("/verify")
    public ResponseEntity<SuccessResponse<CaptchaResponseDTO>> verifyCaptcha(
            @RequestParam("captcha") String captcha,
            HttpSession session) {

        CaptchaResponseDTO result = kaptchaService.verifyCaptchaResult(captcha, session);
        return result.isSuccess()
                ? ApiResponseUtil.success(result)
                : ApiResponseUtil.fail(result.getMessage() ,HttpStatus.BAD_REQUEST);
    }
}