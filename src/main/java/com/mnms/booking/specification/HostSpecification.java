package com.mnms.booking.specification;

import com.mnms.booking.dto.request.HostRequestDTO;
import com.mnms.booking.dto.response.HostResponseDTO;
import com.mnms.booking.exception.global.ErrorResponse;
import com.mnms.booking.exception.global.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "주최자 관련 API", description = "주최자 예매자 명단 조회, 주최자 도메인 데이터 제공 API")
public interface HostSpecification {

    @PostMapping("/list")
    @Operation(
            summary = "주최자 도메인에 예매자 리스트 제공",
            description = "주최자가 FestivalId와 PerformanceDate를 제공하면 해당하는 예매자 userId 리스트를 반환합니다. (프론트와 직접 관련 없음)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "data": [101, 102, 103],
                                      "message": "조회 성공"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "페스티벌을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                               "success": false,
                                               "data": "FESTIVAL_NOT_FOUND",
                                               "message": "입력 ID에 해당하는 페스티벌을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "사용자 API 호출 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                               "success": false,
                                               "data": "USER_API_ERROR",
                                               "message": "예매자 정보를 가져오는데 실패했습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<List<Long>>> getBookingsByOrganizer(
            @Parameter(description = "주최자 요청 DTO", required = true)
            @RequestBody HostRequestDTO hostRequestDTO
    );

    @PostMapping("/booking/list")
    @Operation(
            summary = "예매자 정보 조회",
            description = "예매자 정보를 조회합니다. HOST 또는 ADMIN 권한이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "reservationNumber": "TAEEDA123",
                                          "performanceDate": "2025-09-07T15:00:00",
                                          "userId": 101,
                                          "selectedTicketCount": 2,
                                          "deliveryMethod": "MOBILE",
                                          "address": "서울집",
                                          "userName": "홍길동",
                                          "userPhone": "010-1234-5678"
                                        }
                                      ],
                                      "message": "조회 성공"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "페스티벌을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                               "success": false,
                                               "data": "FESTIVAL_NOT_FOUND",
                                               "message": "입력 ID에 해당하는 페스티벌을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                               "success": false,
                                               "data": "ACCESS_DENIED",
                                               "message": "접근 권한이 없습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "사용자 API 호출 실패 또는 알 수 없는 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                               "success": false,
                                               "data": "USER_API_ERROR",
                                               "message": "예매자 정보를 가져오는데 실패했습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<List<HostResponseDTO>>> getBookingInfo(
            @Parameter(description = "조회할 festivalId", example = "PF123456", required = true)
            @RequestParam String festivalId,

            @Parameter(hidden = true)
            Authentication authentication
    );
}