package com.mnms.booking.util;

import com.mnms.booking.dto.response.ApiResponseDTO;
import com.mnms.booking.dto.response.BookingUserInfoResponseDTO;
import com.mnms.booking.dto.response.BookingUserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Collections;
import java.util.List;


@Component
@RequiredArgsConstructor
public class UserApiClient {

    private final WebClient webClient;

    @Value("${base.service.url}${user.service.email.url}")
    private String userServiceUrl;

    @Value("${base.service.url}${user.service.booking.url}")
    private String bookingUserServiceUrl;

    // userId 리스트 요청
    public List<BookingUserInfoResponseDTO> getUsersByIds(List<Long> userIds) {
        ApiResponseDTO<List<BookingUserInfoResponseDTO>> response = webClient.post()
                .uri(bookingUserServiceUrl)
                .bodyValue(userIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<List<BookingUserInfoResponseDTO>>>() {})
                .block();
        return response != null ? response.getData() : Collections.emptyList();
    }

    // userId로 요청
    public BookingUserResponseDTO getUserInfoById(Long userId) {
        String url = String.format("%s/%d", userServiceUrl, userId);
        ApiResponseDTO<BookingUserResponseDTO> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<BookingUserResponseDTO>>() {
                })
                .block();

        return response != null ? response.getData() : null;
    }
}