package com.mnms.booking.util;

import com.mnms.booking.dto.response.BookingUserInfoResponseDTO;
import com.mnms.booking.dto.response.UserInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserApiClient {

    private final RestTemplate restTemplate;

    @Value("${booking.user.service.url}")
    private String bookingUserServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public List<BookingUserInfoResponseDTO> getUsersByIds(List<Long> userIds) {
        //return restTemplate.postForObject(bookingUserServiceUrl, userIds, List.class);
        ResponseEntity<List<BookingUserInfoResponseDTO>> response =
                restTemplate.exchange(
                        userServiceUrl,
                        HttpMethod.POST,
                        new HttpEntity<>(userIds),
                        new ParameterizedTypeReference<List<BookingUserInfoResponseDTO>>() {}
                );
        return response.getBody();
    }

    public UserInfoResponseDTO getUserInfoById(Long userId) {
        String url = String.format("%s/%d", userServiceUrl, userId);
        return restTemplate.getForObject(url, UserInfoResponseDTO.class);
    }
}