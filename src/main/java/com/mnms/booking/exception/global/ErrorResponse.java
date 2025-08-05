package com.mnms.booking.exception.global;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mnms.booking.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String errorCode;
    private String errorMessage;
}