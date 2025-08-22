package com.mnms.booking.util;

import com.mnms.booking.dto.response.BookingUserInfoResponseDTO;
import com.mnms.booking.dto.response.TransferUserResponse;
import com.mnms.booking.dto.response.UserInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Component
@RequiredArgsConstructor
public class UserApiClient {

    private final WebClient webClient;

    @Value("${booking.user.service.url}")
    private String bookingUserServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;


    // userId 리스트 요청
    public List<BookingUserInfoResponseDTO> getUsersByIds(List<Long> userIds) {
        return webClient.post()
                .uri(bookingUserServiceUrl)
                .bodyValue(userIds)
                .retrieve()
                .bodyToFlux(BookingUserInfoResponseDTO.class)
                .collectList()
                .block();
    }

    // userId로 요청
    public UserInfoResponseDTO getUserInfoById(Long userId) {
        String url = String.format("%s/%d", userServiceUrl, userId);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(UserInfoResponseDTO.class)
                .block();
    }
}