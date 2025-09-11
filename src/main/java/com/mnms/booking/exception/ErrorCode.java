package com.mnms.booking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // USER
    USER_INVALID("U001", "잘못된 사용자 ID 형식입니다", HttpStatus.BAD_REQUEST),
    USER_API_ERROR("U002", "예매자 정보를 가져오는데 실패했습니다.", HttpStatus.CONFLICT),
    UNKNOWN_ERROR("U003", "알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_UNAUTHORIZED_ACCESS("U004", "잘못된 사용자 예매내역 입니다.", HttpStatus.UNAUTHORIZED),
    // 보안문자
    SECURITY_NUMBER_INVALID("S001", "입력한 문자가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // FESTIVAL
    FESTIVAL_NOT_FOUND("F001","입력 ID에 해당하는 페스티벌을 찾을 수 없습니다.",HttpStatus.NOT_FOUND),
    FESTIVAL_INVALID_DATE("F002", "해당 날짜의 페스티벌을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    FESTIVAL_INVALID_TIME("F003", "해당 시간의 페스티벌을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    FESTIVAL_DELIVERY_INVALID("F004", "배송 방법 선택되지 않았습니다.", HttpStatus.BAD_REQUEST),
    FESTIVAL_MISMATCH("F005", "해당하는 QR의 페스티벌 주최자가 아닙니다.", HttpStatus.FORBIDDEN),
    FESTIVAL_LIMIT_AVAILABLE_PEOPLE("F006", "해당 페스티벌 수용 인원이 초과되어 예매를 진행할 수 없습니다.", HttpStatus.CONFLICT),

    // TICKET
    TICKET_ALREADY_RESERVED("T001", "예약 가능한 티켓 수를 초과하였습니다.", HttpStatus.CONFLICT),
    TICKET_NOT_FOUND("T002", "예매 정보가 만료되어 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    TICKET_INVALID_DELIVERY_METHOD("T003", "수령 방법이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    TICKET_DELIVERY_NOT_COMPLETED("T004", "티켓 수령 방법이 선택되지 않았습니다.", HttpStatus.BAD_REQUEST),
    TICKET_FAIL_CANCEL("T005", "티켓 취소가 실패했습니다.", HttpStatus.CONFLICT),
    TICKET_USER_NOT_SAME("T006", "사용자가 티켓 소유자가 아닙니다.", HttpStatus.FORBIDDEN),
    TICKET_ALREADY_CANCELED("T007", "티켓이 이미 예매 취소되었습니다.", HttpStatus.CONFLICT),
    TICKET_EXPIRED("T008", "티켓의 유효기간이 만료되었습니다.", HttpStatus.CONFLICT),
    TICKET_CANCELED("T009", "취소된 티켓입니다.", HttpStatus.CONFLICT),
    TICKET_EMAIL_TEMPLATE_NOT_FOUND("T010", "이메일 템플릿 오류로 이메일 전송에 실패하였습니다.", HttpStatus.NOT_FOUND),

    // QR
    QR_CODE_SAVE_FAILED("Q001", "QR 코드 생성 또는 저장을 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_CODE_ID_GENERATION_FAILED("Q002", "QR 코드 ID 생성을 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_CODE_NOT_FOUND("Q003", "QR 코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QR_CODE_INVALID("Q004", "QR 코드가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    QR_CODE_EXPIRED("Q005", "QR 코드의 만료일이 지났습니다.", HttpStatus.GONE),
    QR_CODE_ALREADY_USED("Q006", "QR 코드가 이미 사용되었습니다.", HttpStatus.CONFLICT),

    // waiting : 대기열 관련 예외
    USER_NOT_FOUND_IN_BOOKING("W001", "사용자가 예매 페이지에 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND_IN_WAITING("W002", "사용자가 대기열에 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    FAILED_TO_ENTER_QUEUE("W003", "대기열 진입에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_TO_REMOVE_USER("W004", "사용자 제거에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REDIS_CONNECTION_FAILED("R001", "Redis 연결에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_TO_EXECUTE_SCRIPT("R002", "대기열 Lua 스크립트 실행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FAILED_TO_ENTER_BOOKING("W005", "예매 진입 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REDIS_PUBLISH_FAILED("R003", "Redis Pub/Sub 발행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    JSON_SERIALIZATION_FAILED("S001", "메시지 직렬화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REDIS_OPERATION_FAILED("R004", "Redis 명령 실행 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // Transfer
    TRANSFER_NOT_VALID_FILE_TYPE("TR001","유효하지 않은 파일 확장자입니다.",HttpStatus.NOT_ACCEPTABLE),
    TRANSFER_NOT_FOUND_INFORM("TR002", "검사에 실패하였습니다.", HttpStatus.NOT_FOUND),
    TRANSFER_NOT_FOUND_NAME("TR003", "이름 검사에 실패하였습니다.", HttpStatus.NOT_FOUND),
    TRANSFER_NOT_FOUND_RRN("TR003", "주민등록 번호 검사에 실패하였습니다.", HttpStatus.NOT_FOUND),
    TRANSFER_NOT_EXIST("TR004", "양도 요청이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    TRANSFER_NOT_MATCH_RECEIVER("TR005", "양도 승인하는 양수자가 맞지 않습니다.", HttpStatus.BAD_REQUEST),
    TRANSFER_NOT_MATCH_TYPE("TR006", "양도 타입이 맞지 않습니다.", HttpStatus.BAD_REQUEST),
    TRANSFER_NOT_MATCH_SENDER("TR007", "양도자의 티켓과 매칭되지 않습니다.", HttpStatus.CONFLICT),
    TRANSFER_ALREADY_EXIST_REQUEST("T008", "진행되고 있는 양도 거래가 존재하거나, 양도 1회 진행한 티켓입니다. 양도는 1회로 제한됩니다.", HttpStatus.CONFLICT),

    // STATISTICS (통계 관련 에러 코드 추가)
    STATISTICS_ACCESS_DENIED("ST001", "통계 정보에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    STATISTICS_NOT_FOUND("ST002", "해당 페스티벌의 통계 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // PAYMENT
    PAYMENT_RESPONSE_ERROR("P001", "결제 응답이 실패하였습니다.", HttpStatus.BAD_REQUEST);

    private final String code;        // A001, A002 등
    private final String message;     // 사용자에게 보여줄 메시지
    private final HttpStatus status;  //http status 코드

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}