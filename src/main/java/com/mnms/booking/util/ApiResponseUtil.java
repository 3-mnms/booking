package com.mnms.booking.util;

import com.mnms.booking.exception.global.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseUtil {
    public static <T> ResponseEntity<SuccessResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(new SuccessResponse<>(true, data, message));
    }

    public static <T> ResponseEntity<SuccessResponse<T>> success(T data) {
        return ResponseEntity.ok(new SuccessResponse<>(true, data, "요청이 성공적으로 처리되었습니다."));
    }

    public static <T> ResponseEntity<SuccessResponse<T>> success() {
        return ResponseEntity.ok(new SuccessResponse<>(true, null, "요청이 성공적으로 처리되었습니다."));
    }

    public static <T> ResponseEntity<SuccessResponse<T>> fail(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new SuccessResponse<>(false, null, message));
    }
}