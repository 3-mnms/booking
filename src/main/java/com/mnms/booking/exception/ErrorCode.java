package com.mnms.booking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // USER
    USER_INVALID("U001", "잘못된 사용자 ID 형식입니다", HttpStatus.BAD_REQUEST),

    // 보안문자
    SECURITY_NUMBER_INVALID("S001", "입력한 문자가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // FESTIVAL
    FESTIVAL_NOT_FOUND("F001","입력 ID에 해당하는 페스티벌을 찾을 수 없습니다.",HttpStatus.NOT_FOUND),
    FESTIVAL_INVALID_DATE("F002", "해당 날짜의 페스티벌을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FESTIVAL_INVALID_TIME("F003", "해당 시간의 페스티벌을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FESTIVAL_DELIVERY_INVALID("F004", "유효하지 않은 배송 방법입니다.", HttpStatus.BAD_REQUEST),
    FESTIVAL_MISMATCH("F005", "해당하는 QR의 페스티벌 주최자가 아닙니다.", HttpStatus.FORBIDDEN),
    FESTIVAL_LIMIT_AVALIABLE_PEOPLE("F006", "해당 페스티벌 수용 인원이 초과되어 예매를 진행할 수 없습니다.", HttpStatus.CONFLICT),
    
    // TICKET
    TICKET_ALREADY_RESERVED("T001", "예약 가능한 티켓 수를 초과하였습니다.", HttpStatus.CONFLICT),
    TICKET_NOT_FOUND("T002", "해당하는 티켓을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TICKET_INVALID_DELIVERY_METHOD("T003", "수령 방법이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    TICKET_DELIVERY_NOT_COMPLETED("T004", "티켓 수령 방법이 선택되지 않았습니다.", HttpStatus.BAD_REQUEST),
    TICKET_FAIL_CANCEL("T005", "티켓 취소가 실패했습니다.", HttpStatus.CONFLICT),
    TICKET_USER_NOT_SAME("T006", "사용자가 티켓 소유자가 아닙니다.", HttpStatus.FORBIDDEN),
    TICKET_ALREADY_CANCELED("T007", "티켓이 이미 예매 취소되었습니다.", HttpStatus.CONFLICT),

    // QR
    QR_CODE_SAVE_FAILED("Q001", "QR 코드 생성 또는 저장을 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_CODE_ID_GENERATION_FAILED("Q002", "QR 코드 ID 생성을 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_CODE_NOT_FOUND("Q003", "QR 코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    QR_CODE_INVALID("Q004", "QR 코드가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    QR_CODE_EXPIRED("Q005", "QR 코드의 만료일이 지났습니다.", HttpStatus.GONE),
    QR_CODE_ALREADY_USED("Q006", "QR 코드가 이미 사용되었습니다.", HttpStatus.CONFLICT),

    // Transfer
    TRANSFER_NOT_VALID_FILE_TYPE("TR001","유효하지 않은 파일 확장자입니다.",HttpStatus.NOT_ACCEPTABLE),
    TRANSFER_NOT_FOUND_INFORM("TR002", "검사에 실패하였습니다.", HttpStatus.NOT_FOUND),
    TRANSFER_NOT_FOUND_NAME("TR003", "이름 검사에 실패하였습니다.", HttpStatus.NOT_FOUND);

    private final String code;        // A001, A002 등
    private final String message;     // 사용자에게 보여줄 메시지
    private final HttpStatus status;  //http status 코드

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}