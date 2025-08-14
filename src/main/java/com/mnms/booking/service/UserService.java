package com.mnms.booking.service;

import com.mnms.booking.dto.response.UserInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class UserService {

    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public UserInfoResponseDTO getUserInfoById(Long userId) {
        String url = String.format("%s/%d", userServiceUrl, userId);
        return restTemplate.getForObject(url, UserInfoResponseDTO.class);
    }
}