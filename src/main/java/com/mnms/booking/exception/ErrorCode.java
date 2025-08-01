package com.mnms.booking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    FESTIVAL_NOT_FOUND("F001","입력 ID에 해당하는 페스티벌을 찾을 수 없습니다.",HttpStatus.NOT_FOUND);


    private final String code;        // A001, A002 등
    private final String message;     // 사용자에게 보여줄 메시지
    private final HttpStatus status;  //http status 코드

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}