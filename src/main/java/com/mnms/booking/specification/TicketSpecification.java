package com.mnms.booking.specification;

import com.mnms.booking.dto.response.TicketDetailResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.exception.global.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "예매 내역 API", description = "예매 내역 조회")
public interface TicketSpecification {

    @GetMapping
    @Operation(summary = "예매한 티켓 정보 조회",
            description = "예매자가 예매 완료한 전체 티켓 리스트를 조회합니다. (status : 완료, 취소한 티켓 조회 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "reservationNumber": "T24CBD629",
                                          "festivalId": "PF272550",
                                          "fname": "그 곳",
                                          "performanceDate": "2025-09-07T15:00:00",
                                          "ticketPrice": 60000,
                                          "deliveryMethod": "MOBILE",
                                          "status": "CONFIRMED"
                                        }
                                      ],
                                      "message": "요청이 성공적으로 처리되었습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "TICKET_NOT_FOUND",
                                      "message": "해당하는 티켓을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<List<TicketResponseDTO>>> getUserTickets(Authentication authentication);

    @GetMapping("/detail")
    @Operation(summary = "예매한 티켓 정보 디테일 조회",
            description = "예매자가 예매 완료한 티켓 정보를 조회합니다. ex : /api/ticket/detail?reservationNumber=T24CBD629 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "reservationNumber": "T24CBD629",
                                        "festivalId": "PF272550",
                                        "fname": "그 곳",
                                        "performanceDate": "2025-09-07T15:00:00",
                                        "ticketPrice": 60000,
                                        "deliveryMethod": "MOBILE",
                                        "status": "CONFIRMED",
                                        "address": "서울집",
                                        "userName": "삼길동"
                                      },
                                      "message": "요청이 성공적으로 처리되었습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "TICKET_NOT_FOUND",
                                      "message": "해당하는 티켓을 찾을 수 없습니다."
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "USER_UNAUTHORIZED_ACCESS",
                                      "message": "잘못된 사용자 예매내역 입니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<TicketDetailResponseDTO>> getUserTicketDetail(
            @RequestParam String reservationNumber,
            Authentication authentication
    );
}
