package com.mnms.booking.specification;

import com.mnms.booking.dto.response.CaptchaResponseDTO;
import com.mnms.booking.exception.global.ErrorResponse;
import com.mnms.booking.exception.global.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Tag(name = "보안문자 API", description = "보안문자 생성 및 인증")
public interface CaptchaSpecification {

    @GetMapping("/image")
    @Operation(
            summary = "보안문자 이미지 요청",
            description = "새로운 보안문자 이미지를 생성하여 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "보안문자 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                                    {
                                      "success": true,
                                      "data": null,
                                      "message": "보안문자 이미지가 생성 완료"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<Void>> getCaptchaImage(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    ) throws IOException;



    @PostMapping("/verify")
    @Operation(
            summary = "보안문자 검증",
            description = "사용자가 입력한 보안문자 값이 올바른지 검증합니다. " +
                    "보안문자는 다섯 글자이며 대소문자 구분하지 않습니다. 만료시간은 3분이고, 불일치로 실패해도 만료시간 내에 입력하면 인증 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "보안문자 인증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "success": true,
                                        "remainingAttempts": 2
                                      },
                                      "message": "보안문자 인증 성공"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "보안문자 인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "실패 응답 예시",
                                    value = """
                                            [
                                                {
                                                    "success": false,
                                                    "data": null,
                                                    "message": "보안문자가 만료되었습니다."
                                                },
                                                {
                                                    "success": false,
                                                    "data": null,
                                                    "message": "보안문자 불일치로 인증이 실패하였습니다."
                                                }
                                            ]
                                            """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<CaptchaResponseDTO>> verifyCaptcha(
            @Parameter(description = "사용자가 입력한 보안문자 값", required = true, example = "aB12c")
            @RequestParam("captcha") String captcha,

            @Parameter(hidden = true)
            HttpSession session
    );
}