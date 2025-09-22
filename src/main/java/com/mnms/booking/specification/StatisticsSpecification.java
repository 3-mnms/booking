package com.mnms.booking.specification;

import com.mnms.booking.dto.response.StatisticsBookingDTO;
import com.mnms.booking.dto.response.StatisticsQrCodeResponseDTO;
import com.mnms.booking.dto.response.StatisticsUserResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "통계 API", description = "공연 별 예매자의 정보를 통해 성별/나이, 입장 인원 상황을 확인 가능")
public interface StatisticsSpecification {

    @Operation(summary = "festivalId별 예매자 성별/나이 통계 조회",
            description = "특정 페스티벌의 예매자 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = StatisticsUserResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "User MSA 조회 실패",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "USER_API_ERROR",
                                      "message": "사용자 통계 정보 조회에 실패했습니다."
                                    }
                                    """
                            )
                    ))
    })
    ResponseEntity<SuccessResponse<StatisticsUserResponseDTO>> getFestivalUserStatistics(String festivalId);



    @Operation(summary = "공연 날짜/시간 목록 조회",
            description = "주최자가 입장 통계를 조회하기 전, 해당 페스티벌의 유효 공연 날짜/시간 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "페스티벌 없음",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "FESTIVAL_NOT_FOUND",
                                      "message": "해당 페스티벌을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    ))
    })
    ResponseEntity<SuccessResponse<List<LocalDateTime>>> getPerformanceDatesForFestival(
            String festivalId,
            Authentication authentication
    );



    @Operation(summary = "공연 날짜별 입장 통계",
            description = "예매자 및 주최자가 공연 날짜별 현장 QR 입장 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = StatisticsQrCodeResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "STATISTICS_ACCESS_DENIED",
                                      "message": "통계 조회 권한이 없습니다."
                                    }
                                    """
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "페스티벌 없음",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "FESTIVAL_NOT_FOUND",
                                      "message": "해당 페스티벌을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "USER_INVALID",
                                      "message": "유효하지 않은 사용자 ID입니다."
                                    }
                                    """
                            )
                    ))
    })
    ResponseEntity<SuccessResponse<StatisticsQrCodeResponseDTO>> getPerformanceEnterStatistics(
            String festivalId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime performanceDate,
            Authentication authentication
    );



    @Operation(summary = "공연별 예매자 수 / 수용 인원 요약 조회",
            description = "주최자가 자신의 페스티벌 공연별 예매 현황과 총 수용 인원을 요약 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "STATISTICS_ACCESS_DENIED",
                                      "message": "통계 조회 권한이 없습니다."
                                    }
                                    """
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "페스티벌 없음",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "FESTIVAL_NOT_FOUND",
                                      "message": "해당 페스티벌을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID",
                    content = @Content(
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "USER_INVALID",
                                      "message": "유효하지 않은 사용자 ID입니다."
                                    }
                                    """
                            )
                    ))
    })
    ResponseEntity<SuccessResponse<List<StatisticsBookingDTO>>> getBookingSummary(
            String festivalId,
            Authentication authentication
    );
}
