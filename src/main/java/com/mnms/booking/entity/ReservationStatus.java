package com.mnms.booking.entity;

public enum ReservationStatus {
    TEMP_RESERVED,     // 가예매 (결제 전)
    CONFIRMED,         // 결제 완료, 확정
    CANCELED,          // 결제 실패 or 취소
}