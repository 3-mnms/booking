package com.mnms.booking.dto.response;

import lombok.Getter;

public class ApiResponseDTO<T> {
    private boolean success;

    @Getter
    private T data;
    private String message;

    // getter, setter
}