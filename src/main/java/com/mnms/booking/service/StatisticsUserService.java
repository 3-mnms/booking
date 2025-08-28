package com.mnms.booking.service;

import com.mnms.booking.dto.response.StatisticsUserResponseDTO;
import com.mnms.booking.exception.BusinessException;
import com.mnms.booking.exception.ErrorCode;
import com.mnms.booking.repository.StatisticsRepository;
import com.mnms.booking.util.StatisticsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsUserService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsUserService.class);
    private final StatisticsRepository statisticsRepository;
    private final WebClient webClient;
    private final String userStatsListApi;

    public StatisticsUserService(
            StatisticsRepository statisticsRepository,
            WebClient.Builder webClientBuilder,
            @Value("${base.service.url}") String baseApiUrl,
            @Value("${user.service.stats.url}") String userStatsListApi
    ) {
        this.statisticsRepository = statisticsRepository;
        this.webClient = webClientBuilder.baseUrl(baseApiUrl).build();
        this.userStatsListApi = userStatsListApi;
    }

    public StatisticsUserResponseDTO getFestivalUserStatistics(String festivalId) {
        List<String> userIds = statisticsRepository.findUserIdsByFestivalId(festivalId);

        if (userIds.isEmpty()) {
            logger.warn("페스티벌 ID: {}에 대한 예매 내역이 없습니다. 통계 수치를 0으로 반환합니다.", festivalId);
            return StatisticsUtil.calculateStatistics(List.of());
        }

        logger.info("페스티벌 ID: {}에 대한 사용자 ID {}개를 찾았습니다.", userIds.size(), festivalId);
        logger.debug("사용자 ID 목록: {}", userIds);

        try {
            List<Map<String, Object>> userDemographics = getUserDemographicsFromUserMSA(userIds);
            logger.info("User MSA로부터 사용자 통계 정보를 받기 성공. 총 건수: {}", userDemographics.size());
            return StatisticsUtil.calculateStatistics(userDemographics);
        } catch (WebClientException e) {
            logger.error("페스티벌 {}에 대한 User MSA 통계 정보 조회에 실패: {}", festivalId, e.getMessage());
            throw new BusinessException(ErrorCode.USER_API_ERROR);
        }
    }

    private List<Map<String, Object>> getUserDemographicsFromUserMSA(List<String> userIds) {
        List<Long> longUserIds = userIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        //logger.info("User MSA API 호출을 시도합니다. URL: {}", this.userStatsListApi);

        return webClient.post()
                .uri(this.userStatsListApi)
                .bodyValue(longUserIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
    }
}