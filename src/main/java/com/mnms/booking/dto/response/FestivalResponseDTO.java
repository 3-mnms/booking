package com.mnms.booking.dto.response;

import lombok.Data;

@Data
public class FestivalResponseDTO {

    private String mt20id;

    // 공연명
    private String prfnm;

    // 공연 시작일
    private String prfpdfrom;

    // 공연 종료일
    private String prfpdto;

    // 공연 시설명
    private String fcltynm;

    // 포스터 이미지 경로
    private String poster;

    // 티켓 비용
    private String pcseguidance;

    // 수용 가능 인원
    private int availableNOP;

    // 택배 날짜 (지류티켓 선택 시, 페스티벌 날짜 2주일 전 배송 - 자동) - 추후
}
