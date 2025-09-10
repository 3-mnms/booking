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
                            implementation = SuccessResponse.class,
                            example = "{\n" +
                                    "  \"success\": true,\n" +
                                    "  \"data\": {\n" +
                                    "    \"festivalId\": \"PF272550\",\n" +
                                    "    \"fname\": \"그 곳\",\n" +
                                    "    \"performanceDate\": \"2025-09-07T15:00:00\",\n" +
                                    "    \"posterFile\": \"http://www.kopis.or.kr/upload/pfmPoster/PF_PF272550_250825_154032.gif\",\n" +
                                    "    \"ticketPrice\": 60000\n" +
                                    "  },\n" +
                                    "  \"message\": \"요청이 성공적으로 처리되었습니다.\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "404", description = "페스티벌을 찾을 수 없음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{\n" +
                                    "  \"success\": false,\n" +
                                    "  \"data\": null,\n" +
                                    "  \"message\": \"페스티벌을 찾을 수 없습니다.\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "400", description = "선택한 날짜/시간이 잘못됨",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{\n" +
                                    "  \"success\": false,\n" +
                                    "  \"data\": null,\n" +
                                    "  \"message\": \"선택한 날짜/시간이 잘못되었습니다.\"\n" +
                                    "}"
                    )))
    })
    ResponseEntity<SuccessResponse<FestivalDetailResponseDTO>> getFestivalDetail(
            @Valid @RequestBody BookingSelectRequestDTO request);

    /// POST : 2차 예매 상세 조회
    @PostMapping("/detail/phases/2")
    @Operation(summary = "2차 : 예매 단계에서 선택한 예매 상세 조회",
            description = "festivalId, reservationNumber로 공연 및 예매자 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class,
                            example = "{\n" +
                                    "  \"success\": true,\n" +
                                    "  \"data\": {\n" +
                                    "    \"reservationNumber\": \"TAEEDA779\",\n" +
                                    "    \"userName\": \"삼길동\",\n" +
                                    "    \"performanceDate\": \"2025-09-07T15:00:00\",\n" +
                                    "    \"deliveryMethod\": \"MOBILE\",\n" +
                                    "    \"address\": \"서울집\",\n" +
                                    "    \"ticketPrice\": 60000\n" +
                                    "  },\n" +
                                    "  \"message\": \"조회 성공\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "404", description = "페스티벌 또는 티켓을 찾을 수 없음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"페스티벌 또는 티켓을 찾을 수 없습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"인증에 실패했습니다.\" }"
                    )))
    })
    ResponseEntity<SuccessResponse<BookingDetailResponseDTO>> getFestivalBookingDetail(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication
    );

    /// POST : 날짜 선택
    @PostMapping("/selectDate")
    @Operation(summary = "페스티벌 날짜, 시간, 매수 선택",
            description = "festivalId, performanceDate, selectedTicketCount를 입력하고 reservationNumber 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "임시 예약 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class,
                            example = "{ \"success\": true, \"data\": \"TAEEDA779\", \"message\": \"임시 예약 성공\" }"
                    ))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 날짜/시간 또는 티켓 수 초과",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"유효하지 않은 날짜/시간 또는 티켓 수 초과\" }"
                    ))),
            @ApiResponse(responseCode = "404", description = "페스티벌을 찾을 수 없음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"페스티벌을 찾을 수 없습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "409", description = "예약 가능한 티켓 수를 초과",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"예약 가능한 티켓 수를 초과했습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"인증에 실패했습니다.\" }"
                    )))
    })
    ResponseEntity<SuccessResponse<String>> selectFestivalDate(
            @Valid @RequestBody BookingSelectRequestDTO request,
            Authentication authentication
    );

    /// POST : 배송 선택
    @PostMapping("/selectDeliveryMethod")
    @Operation(summary = "페스티벌 티켓 수령 방법, 주소 선택",
            description = "festivalId, performanceDate, deliveryMethod(MOBILE or PAPER), address 선택")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배송 방법 선택 완료",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class,
                            example = "{ \"success\": true, \"data\": null, \"message\": \"배송 방법 선택 완료\" }"
                    ))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 배송 방법/주소",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"유효하지 않은 배송 방법/주소\" }"
                    ))),
            @ApiResponse(responseCode = "404", description = "티켓 또는 페스티벌을 찾을 수 없음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"티켓 또는 페스티벌을 찾을 수 없습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"인증에 실패했습니다.\" }"
                    )))
    })
    ResponseEntity<SuccessResponse<Void>> selectFestivalDelivery(
            @Valid @RequestBody BookingSelectDeliveryRequestDTO request,
            Authentication authentication
    );

    /// POST : 3차 예매 완료 (QR 생성)
    @PostMapping("/qr")
    @Operation(summary = "페스티벌 예매 티켓 생성",
            description = "사용자가 특정 페스티벌 티켓을 예약하기 위한 마지막 가예매 상태")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR 생성 및 예약 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class,
                            example = "{ \"success\": true, \"data\": null, \"message\": \"QR 생성 및 예약 성공\" }"
                    ))),
            @ApiResponse(responseCode = "400", description = "배송 방법 선택하지 않거나 올바르지 않음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"배송 방법 선택하지 않았거나 올바르지 않습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "404", description = "티켓 또는 페스티벌을 찾을 수 없음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"티켓 또는 페스티벌을 찾을 수 없습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"인증에 실패했습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "409", description = "페스티벌 수용 인원 초과",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"페스티벌 수용 인원을 초과했습니다.\" }"
                    )))
    })
    ResponseEntity<SuccessResponse<Void>> reserveTicket(
            @Valid @RequestBody BookingRequestDTO request,
            Authentication authentication
    );

    /// GET : WebSocket 메시지 누락 방지
    @GetMapping("reservation/status")
    @Operation(summary = "예매 완료/취소 정보 조회",
            description = "WebSocket 메시지 누락 시 상태 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class,
                            example = "{ \"success\": true, \"data\": \"CONFIRMED\", \"message\": \"조회 성공\" }"
                    ))),
            @ApiResponse(responseCode = "404", description = "티켓을 찾을 수 없음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"티켓을 찾을 수 없습니다.\" }"
                    )))
    })
    ResponseEntity<SuccessResponse<ReservationStatus>> checkStatus(@RequestParam String reservationNumber);

    /// GET : 예매자 정보 조회
    @GetMapping("/user/info")
    @Operation(summary = "예매자 정보 조회",
            description = "예매자 role이 user인 사람만 조회 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class,
                            example = "{\n" +
                                    "  \"success\": true,\n" +
                                    "  \"data\": {\n" +
                                    "    \"userName\": \"삼길동\",\n" +
                                    "    \"id\": 12\n" +
                                    "  },\n" +
                                    "  \"message\": \"조회 성공\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"인증에 실패했습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"권한이 없습니다.\" }"
                    )))
    })
    ResponseEntity<SuccessResponse<BookingUserResponseDTO>> getUserInfo(Authentication authentication);

    /// POST : 이메일 임시 테스트
    @PostMapping("/email/test")
    @Operation(summary = "[테스트 진행X] 이메일 임시 테스트",
            description = "예매 완료 후 이메일 전송 임시 테스트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메일 발송 성공",
                    content = @Content(schema = @Schema(
                            implementation = SuccessResponse.class,
                            example = "{ \"success\": true, \"data\": null, \"message\": \"메일 발송 성공\" }"
                    ))),
            @ApiResponse(responseCode = "404", description = "티켓을 찾을 수 없음",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"티켓을 찾을 수 없습니다.\" }"
                    ))),
            @ApiResponse(responseCode = "500", description = "메일 발송 실패",
                    content = @Content(schema = @Schema(
                            implementation = ErrorResponse.class,
                            example = "{ \"success\": false, \"data\": null, \"message\": \"메일 발송 실패\" }"
                    )))
    })
    ResponseEntity<String> confirmTicket(
            @RequestParam String reservationNumber,
            @RequestParam boolean paymentStatus
    );
}
