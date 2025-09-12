package com.mnms.booking.specification;

import com.mnms.booking.dto.request.TicketTransferRequestDTO;
import com.mnms.booking.dto.request.UpdateTicketRequestDTO;
import com.mnms.booking.dto.response.PersonInfoResponseDTO;
import com.mnms.booking.dto.response.TicketResponseDTO;
import com.mnms.booking.dto.response.TicketTransferResponseDTO;
import com.mnms.booking.dto.response.TransferOthersResponseDTO;
import com.mnms.booking.exception.global.ErrorResponse;
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
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Tag(name = "양도 API", description = "양도 및 OCR, Ticket 재생성")
public interface TransferSpecification {

    ///  양도할 수 있는 티켓 조회
    @Operation(summary = "양도 가능한 티켓 정보 조회",
            description = "사용자가 양도 가능한 티켓을 조회할 수 있습니다. (status : 양도 받은 티켓 양도 불가능)"
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
            @ApiResponse(responseCode = "404", description = "티켓 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                      "success": false,
                                      "data": "TICKET_NOT_FOUND",
                                      "message": "예매 티켓이 존재하지 않습니다."
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<List<TicketResponseDTO>>> getUserTickets(Authentication authentication);



    ///  가족관계증명서 인증
    @Operation(summary = "가족 간 양도 인증 시도",
            description = "가족관계증명서 PDF와 양도자/양수자 정보로 인증을 시도합니다."
    )
    @ApiResponse(responseCode = "406", description = "유효하지 않은 파일 첨부",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": false,
                                      "data": "TRANSFER_NOT_VALID_FILE_TYPE",
                                      "message": "유효하지 않은 파일 확장자입니다."
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<SuccessResponse<Void>> extractPersonAuth(
            @RequestPart("file") MultipartFile file,
            @RequestPart("targetInfo") String targetInfoJson
    ) throws IOException;



    @Operation(summary = "가족 간 양도 인증 결과 조회",
            description = "인증 완료 후 양도 대상 정보와 함께 반환합니다."
    )
    @ApiResponse(responseCode = "406", description = "유효하지 않은 파일 첨부",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "success": false,
                                      "data": "TRANSFER_NOT_VALID_FILE_TYPE",
                                      "message": "유효하지 않은 파일 확장자입니다."
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<SuccessResponse<List<PersonInfoResponseDTO>>> extractPersonInfo(
            @RequestPart("file") MultipartFile file,
            @RequestPart("targetInfo") String targetInfoJson
    ) throws IOException;



    @Operation(summary = "양도 요청",
            description = "양도자가 양도 요청을 보냅니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "티켓 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
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
            @ApiResponse(responseCode = "409", description = "이미 양도 요청 존재",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TRANSFER_ALREADY_EXIST_REQUEST",
                                  "message": "진행되고 있는 양도 거래가 존재하거나, 양도 1회 진행한 티켓입니다. 양도는 1회로 제한됩니다."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음 (티켓 소유자 불일치)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_USER_NOT_SAME",
                                  "message": "사용자가 티켓 소유자가 아닙니다."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "알 수 없는 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "INTERNAL_SERVER_ERROR",
                                  "message": "알 수 없는 오류가 발생했습니다."
                                }
                                """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<Void>> requestTransfer(
            @RequestBody TicketTransferRequestDTO dto,
            Authentication authentication
    );



    @Operation(summary = "양도 요청 조회",
            description = "양수자가 자신에게 온 양도 요청을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "양도 요청이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TRANSFER_NOT_EXIST",
                                  "message": "양도 요청이 존재하지 않습니다."
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "티켓 없음",
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
            @ApiResponse(responseCode = "404", description = "페스티벌 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
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
            @ApiResponse(responseCode = "500", description = "알 수 없는 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "INTERNAL_SERVER_ERROR",
                                  "message": "알 수 없는 오류가 발생했습니다."
                                }
                                """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<List<TicketTransferResponseDTO>>> watchTransfer(Authentication authentication);



    /// 가족 간 양도 요청 승인
    @Operation(
            summary = "가족 간 양도 요청 수락",
            description = "양수자가 요청을 수락하면 티켓과 QR 정보가 업데이트 됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "양도 타입 또는 양수자 매칭 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TRANSFER_NOT_MATCH_TYPE",
                                          "message": "양도 타입이 맞지 않습니다."
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TRANSFER_NOT_MATCH_RECEIVER",
                                          "message": "양도 승인하는 양수자가 맞지 않습니다."
                                        }
                                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "404", description = "티켓 또는 양도 요청 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TRANSFER_NOT_EXIST",
                                          "message": "양도 요청이 존재하지 않습니다."
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TICKET_NOT_FOUND",
                                          "message": "해당하는 티켓을 찾을 수 없습니다."
                                        }
                                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "409", description = "티켓 상태 문제",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TICKET_EXPIRED",
                                          "message": "티켓의 유효기간이 만료되었습니다."
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TICKET_CANCELED",
                                          "message": "취소된 티켓입니다."
                                        }
                                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "알 수 없는 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "INTERNAL_SERVER_ERROR",
                                  "message": "알 수 없는 오류가 발생했습니다."
                                }
                                """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<Void>> responseTicketFamily(
            @RequestBody UpdateTicketRequestDTO request,
            Authentication authentication
    );


    /// 지인간 양도 요청 승인
    @Operation(summary = "타인 간 양도 요청 완료",
            description = "양수자가 요청을 수락하면 결제 진행 후 양도 완료됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "양도 타입 또는 양수자 매칭 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TRANSFER_NOT_MATCH_TYPE",
                                          "message": "양도 타입이 맞지 않습니다."
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TRANSFER_NOT_MATCH_RECEIVER",
                                          "message": "양도 승인하는 양수자가 맞지 않습니다."
                                        }
                                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "404", description = "티켓 또는 양도 요청 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TRANSFER_NOT_EXIST",
                                          "message": "양도 요청이 존재하지 않습니다."
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            value = """
                                        {
                                          "success": false,
                                          "data": "TICKET_NOT_FOUND",
                                          "message": "해당하는 티켓을 찾을 수 없습니다."
                                        }
                                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "알 수 없는 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "INTERNAL_SERVER_ERROR",
                                  "message": "알 수 없는 오류가 발생했습니다."
                                }
                                """
                            )
                    )
            )
    })
    ResponseEntity<SuccessResponse<TransferOthersResponseDTO>> responseTicketOthers(
            @RequestBody UpdateTicketRequestDTO request,
            Authentication authentication
    );


    ///  Websocket 메시지 누락 방지 api요청
    @Operation(summary = "양도 결제 완료 조회",
            description = "Websocket 메시지 누락 시 양도 완료 여부 확인"
    )
    ResponseEntity<SuccessResponse<Boolean>> checkStatus(@RequestParam Long transferId);
}