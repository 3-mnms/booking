package com.mnms.booking.specification;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "QR API", description = "QR 이미지 조회, 스캔")
public interface QrCodeSpecification {


    @Operation(summary = "QR 코드 이미지 조회", description = "qrCodeId로 QR 코드 이미지를 PNG 형식으로 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "image/png")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "QR 코드를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "success": false,
                                               "data": "QR_CODE_ALREADY_USED",
                                               "message": "이미 사용된 QR 코드입니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<byte[]> getQrCodeImage(
            @Parameter(description = "조회할 QR 코드 ID", example = "4b2f23d3019b727a3320f5a79ae98d27")
            @PathVariable String qrCodeId
    );



    @Operation(summary = "QR 코드 스캔 및 유효성 검사", description = "qrCodeId와 사용자 ID로 QR 코드 유효성 검사 후 QR 사용 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "QR 스캔 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "410",
                    description = "QR 코드가 유효하지 않거나 만료됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Expired QR",
                                    value = """
                                            {
                                               "success": false,
                                               "data": "QR_CODE_EXPIRED",
                                               "message": "QR 코드의 만료일이 지났습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "주최자와 QR 코드 페스티벌 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "success": false,
                                               "data": "FESTIVAL_MISMATCH",
                                               "message": "해당하는 QR의 페스티벌 주최자가 아닙니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 사용된 QR 코드",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "success": false,
                                               "data": "QR_CODE_ALREADY_USED",
                                               "message": "QR 코드가 이미 사용되었습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<Void>> validateAndUseQrCode(
            @PathVariable String qrCodeId,
            Authentication authentication
    );
}
