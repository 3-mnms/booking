package com.mnms.booking.specification;

import com.mnms.booking.dto.response.WaitingNumberResponseDTO;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Tag(name = "대기열 API", description = "대기열 입장, 예매 화면 입장, 대기번호 조회")
public interface WaitingSpecification {


        @Operation(
                summary = "예매 페이지 입장 요청",
                description = "사용자가 예매 페이지에 입장하거나 대기열에 등록됩니다. " +
                        "즉시 입장이 가능하면 waitingNumber는 0, 대기열 입장 시 1 이상의 대기번호 반환."
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "대기열 입장 성공 또는 예매 페이지 바로 입장",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = SuccessResponse.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "페스티벌이 존재하지 않거나 대기열에서 사용자 조회 실패",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class),
                                examples = {
                                        @ExampleObject(
                                                name = "FESTIVAL_NOT_FOUND",
                                                value = """
                                    {
                                      "success": false,
                                      "data": "FESTIVAL_NOT_FOUND",
                                      "message": "입력 ID에 해당하는 페스티벌을 찾을 수 없습니다."
                                    }
                                    """
                                        ),
                                        @ExampleObject(
                                                name = "USER_NOT_FOUND_IN_WAITING",
                                                value = """
                                    {
                                      "success": false,
                                      "data": "USER_NOT_FOUND_IN_WAITING",
                                      "message": "대기열에서 사용자를 찾을 수 없습니다."
                                    }
                                    """
                                        )
                                }
                        )
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "예약 또는 대기열 입장 실패",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class),
                                examples = {
                                        @ExampleObject(
                                                name = "FAILED_TO_ENTER_BOOKING",
                                                value = """
                                    {
                                      "success": false,
                                      "data": "FAILED_TO_ENTER_BOOKING",
                                      "message": "예매 진입 처리에 실패했습니다."
                                    }
                                    """
                                        ),
                                        @ExampleObject(
                                                name = "FAILED_TO_ENTER_QUEUE",
                                                value = """
                                    {
                                      "success": false,
                                      "data": "FAILED_TO_ENTER_QUEUE",
                                      "message": "대기열 진입에 실패했습니다."
                                    }
                                    """
                                        ),
                                        @ExampleObject(
                                                name = "REDIS_CONNECTION_FAILED",
                                                value = """
                                    {
                                      "success": false,
                                      "data": "REDIS_CONNECTION_FAILED",
                                      "message": "Redis 서버 연결에 실패했습니다."
                                    }
                                    """
                                        ),
                                        @ExampleObject(
                                                name = "FAILED_TO_EXECUTE_SCRIPT",
                                                value = """
                                    {
                                      "success": false,
                                      "data": "FAILED_TO_EXECUTE_SCRIPT",
                                      "message": "Redis 스크립트 실행에 실패했습니다."
                                    }
                                    """
                                        ),
                                        @ExampleObject(
                                                name = "REDIS_PUBLISH_FAILED",
                                                value = """
                                    {
                                      "success": false,
                                      "data": "REDIS_PUBLISH_FAILED",
                                      "message": "Redis Pub/Sub 발행 실패"
                                    }
                                    """
                                        )
                                }
                        )
                )
        })
        ResponseEntity<SuccessResponse<WaitingNumberResponseDTO>> enterBookingPage(
                @RequestParam String festivalId,
                @RequestParam LocalDateTime reservationDate,
                @Parameter(hidden = true) Authentication authentication
        );



        @Operation(
                summary = "예매 페이지에서 사용자 퇴장 처리 (예매 완료 또는 타임아웃)",
                description = "예매 페이지에 있던 사용자가 퇴장했을 때 실행됩니다. " +
                        "대기열에 있던 대기번호 1번 사용자는 스케쥴러에 의해 예매 페이지로 자동 입장하며, " +
                        "대기열에 있던 모든 대기자의 대기번호가 변경됩니다."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "사용자 퇴장 성공",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = SuccessResponse.class)
                        )
                ),
                @ApiResponse(responseCode = "404", description = "예매 사용자 목록에 없음",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = SuccessResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                    {
                                      "success": false,
                                      "data": "USER_NOT_FOUND_IN_BOOKING",
                                      "message": "사용자가 예매 페이지에 존재하지 않습니다."
                                    }
                                    """
                                )
                        )
                ),
                @ApiResponse(responseCode = "500", description = "서버 오류",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = SuccessResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                    {
                                      "success": false,
                                      "data": "INTERNAL_SERVER_ERROR",
                                      "message": "서버 오류로 인해 사용자를 처리하지 못했습니다."
                                    }
                                    """
                                )
                        )
                )
        })
        ResponseEntity<SuccessResponse<String>> releaseUser(
                @RequestParam String festivalId,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reservationDate,
                @Parameter(hidden = true) Authentication authentication
        );




    @Operation(
            summary = "대기열 퇴장",
            description = "대기 중인 사용자가 스스로 대기열에서 나갈 때 호출됩니다. " +
                    "호출 시 해당 사용자는 대기열에서 제거되고, 남은 대기자에게 알림이 전송됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 퇴장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "대기열에 사용자 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "USER_NOT_FOUND_IN_WAITING",
                                  "message": "해당 사용자는 대기열 목록에 없습니다."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INTERNAL_SERVER_ERROR",
                                            value = """
                                {
                                  "success": false,
                                  "data": "INTERNAL_SERVER_ERROR",
                                  "message": "서버 오류로 인해 사용자를 처리하지 못했습니다."
                                }
                                """
                                    ),
                                    @ExampleObject(
                                            name = "REDIS_CONNECTION_FAILED",
                                            value = """
                                {
                                  "success": false,
                                  "data": "REDIS_CONNECTION_FAILED",
                                  "message": "대기열 정보를 처리하는 중 Redis 연결에 실패했습니다."
                                }
                                """
                                    ),
                                    @ExampleObject(
                                            name = "REDIS_PUBLISH_FAILED",
                                            value = """
                                {
                                  "success": false,
                                  "data": "REDIS_PUBLISH_FAILED",
                                  "message": "Redis Pub/Sub 발행 실패"
                                }
                                """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<SuccessResponse<String>> exitWaitingUser(
            @Parameter(description = "페스티벌 ID", required = true, example = "festival-001")
            @RequestParam String festivalId,

            @Parameter(description = "예매 날짜 및 시간", required = true, example = "2025-09-10T18:30:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime reservationDate,

            @Parameter(hidden = true)
            Authentication authentication
    );
}
