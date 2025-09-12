package com.mnms.booking.specification;

import com.mnms.booking.dto.request.BookingRequestDTO;
import com.mnms.booking.dto.request.BookingSelectDeliveryRequestDTO;
import com.mnms.booking.dto.request.BookingSelectRequestDTO;
import com.mnms.booking.dto.response.BookingDetailResponseDTO;
import com.mnms.booking.dto.response.BookingUserResponseDTO;
import com.mnms.booking.dto.response.FestivalDetailResponseDTO;
import com.mnms.booking.enums.ReservationStatus;
import com.mnms.booking.exception.global.ErrorResponse;
import com.mnms.booking.exception.global.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "예매 API", description = "예매 티켓 조회, 티켓 선택, 생성")
public interface BookingSpecification {

    /// GET : 페스티벌 예매 정보 조회
    @PostMapping("/detail/phases/1")
    @Operation(summary = "1차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId와 performanceDate로 공연 상세 정보를 조회합니다. selectedTicketCount는 0으로 넣을 것!")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class
                    ))),
            @ApiResponse(responseCode = "404", description = "페스티벌을 찾을 수 없음",
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
                    ))),
            @ApiResponse(responseCode = "400", description = "선택한 날짜/시간이 잘못됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "FESTIVAL_INVALID_DATE / FESTIVAL_INVALID_TIME",
                                  "message": "해당 날짜/시간의 페스티벌을 찾을 수 없습니다."
                                }
                                """
                            )))

    })
    ResponseEntity<SuccessResponse<FestivalDetailResponseDTO>> getFestivalDetail(
            @Valid @RequestBody BookingSelectRequestDTO request);

    /// POST : 2차 예매 상세 조회
    @Operation(summary = "2차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId, reservationNumber로 공연 및 예매자 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class
                    ))),
            @ApiResponse(responseCode = "404", description = "페스티벌을 찾을 수 없음",
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
                            ))),
            @ApiResponse(responseCode = "404", description = "예매 중인 티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_NOT_FOUND",
                                  "message": "예매 정보가 만료되어 존재하지 않습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "UNAUTHORIZED",
                                  "message": "X-User-Id 또는 X-User-Role 헤더가 없습니다. OR X-User-Id가 숫자가 아닙니다."
                                }
                                """
                            )))
    })
    ResponseEntity<SuccessResponse<BookingDetailResponseDTO>> getFestivalBookingDetail(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication
    );

    /// POST : 날짜 선택
    @Operation(summary = "페스티벌 날짜, 시간, 매수 선택",
            description = "festivalId, performanceDate, selectedTicketCount를 입력하고 reservationNumber 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "임시 예약 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class
                    ))),
            @ApiResponse(responseCode = "404", description = "페스티벌을 찾을 수 없음",
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
                            ))),
            @ApiResponse(responseCode = "404", description = "예매 중인 티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_NOT_FOUND",
                                  "message": "예매 정보가 만료되어 존재하지 않습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "400", description = "페스티벌 해당 날짜 또는 시간 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "FESTIVAL_INVALID_DATE OR FESTIVAL_INVALID_TIME",
                                  "message": "해당 날짜/시간의 페스티벌을 찾을 수 없습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "409", description = "예약 가능한 티켓 수 초과",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_ALREADY_RESERVED",
                                  "message": "예약 가능한 티켓 수를 초과하였습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "UNAUTHORIZED",
                                  "message": "X-User-Id 또는 X-User-Role 헤더가 없습니다. OR X-User-Id가 숫자가 아닙니다."
                                }
                                """
                            )))
    })
    ResponseEntity<SuccessResponse<String>> selectFestivalDate(
            @Valid @RequestBody BookingSelectRequestDTO request,
            Authentication authentication
    );

    /// POST : 배송 선택
    @Operation(summary = "페스티벌 티켓 수령 방법, 주소 선택",
            description = "festivalId, performanceDate, deliveryMethod(MOBILE or PAPER), address 선택")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배송 방법 선택 완료",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class
                    ))),
            @ApiResponse(responseCode = "400", description = "배송 방법 미선택",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "FESTIVAL_DELIVERY_INVALID",
                                  "message": "배송 방법 선택되지 않았습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "400", description = "수령 방법",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_INVALID_DELIVERY_METHOD",
                                  "message": "수령 방법이 올바르지 않습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "404", description = "페스티벌을 찾을 수 없음",
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
                            ))),
            @ApiResponse(responseCode = "404", description = "예매 중인 티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_NOT_FOUND",
                                  "message": "예매 정보가 만료되어 존재하지 않습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "400", description = "페스티벌 해당 날짜 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "FESTIVAL_INVALID_DATE",
                                  "message": "해당 날짜/시간의 페스티벌을 찾을 수 없습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "UNAUTHORIZED",
                                  "message": "X-User-Id 또는 X-User-Role 헤더가 없습니다. OR X-User-Id가 숫자가 아닙니다."
                                }
                                """
                            )))
    })
    ResponseEntity<SuccessResponse<Void>> selectFestivalDelivery(
            @Valid @RequestBody BookingSelectDeliveryRequestDTO request,
            Authentication authentication
    );

    /// POST : 3차 예매 완료 (QR 생성)
    @Operation(summary = "페스티벌 예매 티켓 생성",
            description = "사용자가 특정 페스티벌 티켓을 예약하기 위한 마지막 가예매 상태")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR 생성 및 예약 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class
                    ))),
            @ApiResponse(responseCode = "404", description = "페스티벌을 찾을 수 없음",
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
                            ))),
            @ApiResponse(responseCode = "404", description = "예매 중인 티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_NOT_FOUND",
                                  "message": "예매 정보가 만료되어 존재하지 않습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "409", description = "페스티벌 수용 인원 초과",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "FESTIVAL_LIMIT_AVAILABLE_PEOPLE",
                                  "message": "해당 페스티벌 수용 인원이 초과되어 예매를 진행할 수 없습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "UNAUTHORIZED",
                                  "message": "X-User-Id 또는 X-User-Role 헤더가 없습니다. OR X-User-Id가 숫자가 아닙니다."
                                }
                                """
                            )))
    })
    ResponseEntity<SuccessResponse<Void>> reserveTicket(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication
    );

    /// GET : WebSocket 메시지 누락 방지
    @Operation(summary = "예매 완료/취소 정보 조회",
            description = "WebSocket 메시지 누락 시 상태 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class
                    ))),
            @ApiResponse(responseCode = "404", description = "예매 중인 티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_NOT_FOUND",
                                  "message": "예매 정보가 만료되어 존재하지 않습니다."
                                }
                                """
                            )))
    })
    ResponseEntity<SuccessResponse<ReservationStatus>> checkStatus(@RequestParam String reservationNumber);

    /// GET : 예매자 정보 조회
    @Operation(summary = "예매자 정보 조회",
            description = "예매자 role이 user인 사람만 조회 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class
                    ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "UNAUTHORIZED",
                                  "message": "X-User-Id 또는 X-User-Role 헤더가 없습니다. OR X-User-Id가 숫자가 아닙니다."
                                }
                                """
                            )))
    })
    ResponseEntity<SuccessResponse<BookingUserResponseDTO>> getUserInfo(Authentication authentication);


    /// POST : 이메일 임시 테스트
    @Operation(summary = "[테스트 진행X] 이메일 임시 테스트",
            description = "예매 완료 후 이메일 전송 임시 테스트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메일 발송 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class
                    ))),
            @ApiResponse(responseCode = "404", description = "예매 중인 티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_NOT_FOUND",
                                  "message": "예매 정보가 만료되어 존재하지 않습니다."
                                }
                                """
                            ))),
            @ApiResponse(responseCode = "404", description = "예매 중인 티켓을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                {
                                  "success": false,
                                  "data": "TICKET_EMAIL_TEMPLATE_NOT_FOUND",
                                  "message": "이메일 템플릿 오류로 이메일 전송에 실패하였습니다."
                                }
                                """
                            )))
    })
    ResponseEntity<String> confirmTicket(
            @RequestParam String reservationNumber,
            @RequestParam boolean paymentStatus
    );
}
