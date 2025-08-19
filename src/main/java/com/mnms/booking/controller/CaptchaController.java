package com.mnms.booking.controller;

import com.mnms.booking.dto.response.CaptchaResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import com.mnms.booking.service.CaptchaService;
import com.mnms.booking.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "보안문자 API", description = "보안문자 생성 및 인증")
public class CaptchaController {

    private final CaptchaService kaptchaService;

    @GetMapping("/image")
    @Operation(
            summary = "보안문자 이미지 요청",
            description = "새로운 보안문자 이미지를 생성하여 반환합니다."
    )
    public ResponseEntity<SuccessResponse<Void>> getCaptchaImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        kaptchaService.writeCaptchaImage(request.getSession(), response);
        return ApiResponseUtil.success(null, "보안문자 이미지가 생성 완료");
    }

    @PostMapping("/verify")
    @Operation(
            summary = "보안문자 검증",
            description = "사용자가 입력한 보안문자 값이 올바른지 검증합니다. " +
                    "보안문자는 다섯 글자이며 대소문자 구분하지 않습니다. 만료시간은 3분이고, 불일치로 실패해도 만료시간 내에 입력하면 인증 가능합니다."
    )
    public ResponseEntity<SuccessResponse<CaptchaResponseDTO>> verifyCaptcha(
            @RequestParam("captcha") String captcha,
            HttpSession session) {

        CaptchaResponseDTO result = kaptchaService.verifyCaptchaResult(captcha, session);
        return result.isSuccess()
                ? ApiResponseUtil.success(result)
                : ApiResponseUtil.fail(result.getMessage() ,HttpStatus.BAD_REQUEST);
    }
}