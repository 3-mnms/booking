package com.mnms.booking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // FESTIVAL
    FESTIVAL_NOT_FOUND("F001","입력 ID에 해당하는 페스티벌을 찾을 수 없습니다.",HttpStatus.NOT_FOUND),
    FESTIVAL_INVALID_DATE("F002", "공연 시작일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FESTIVAL_DELIVERY_INVALID("F003", "유효하지 않은 배송 방법입니다.", HttpStatus.BAD_REQUEST),

    // TICKET
    TICKET_ALREADY_RESERVED("T001", "예약 가능한 티켓 수를 초과하였습니다.", HttpStatus.CONFLICT),

    // QR
    QR_CODE_SAVE_FAILED("Q001", "QR 코드 생성 또는 저장을 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_CODE_ID_GENERATION_FAILED("Q002", "QR 코드 ID 생성을 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_IMAGE_GENERATION_FAILED("Q003", "QR 이미지 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_IMAGE_CONVERSION_FAILED("Q004", "QR 이미지 데이터를 변환하는 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_PAYLOAD_SERIALIZATION_FAILED("Q005", "QR 코드 데이터를 직렬화하는 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;        // A001, A002 등
    private final String message;     // 사용자에게 보여줄 메시지
    private final HttpStatus status;  //http status 코드

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}