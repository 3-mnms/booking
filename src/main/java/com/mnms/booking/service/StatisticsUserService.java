package com.mnms.booking.service;

import com.mnms.booking.repository.StatisticsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.DecimalFormat;

@Service
public class StatisticsUserService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsUserService.class);
    private final StatisticsRepository statisticsRepository;
    private final WebClient webClient;
    private final String userStatsListApi;
    private static final DecimalFormat df = new DecimalFormat("0.00");

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

    public Map<String, Object> getFestivalStatistics(String festivalId) {
        List<String> userIds = statisticsRepository.findUserIdsByFestivalId(festivalId);

        if (userIds.isEmpty()) {
            logger.warn("No bookings found for festival ID: {}. Returning zero statistics.", festivalId);
            return calculateStatistics(List.of());
        }

        logger.info("Found {} user IDs for festival ID: {}", userIds.size(), festivalId);
        logger.debug("User IDs: {}", userIds);

        List<Map<String, Object>> userDemographics = getUserDemographicsFromUserMSA(userIds);
        logger.info("Received user demographics from User MSA. Count: {}", userDemographics.size());

        Map<String, Object> statistics = calculateStatistics(userDemographics);

        return statistics;
    }

    private List<Map<String, Object>> getUserDemographicsFromUserMSA(List<String> userIds) {
        List<Long> longUserIds = userIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // WebClient 호출 직전 로그 추가
        logger.info("Attempting to call User MSA API at: {}", this.userStatsListApi);
        try {
            return webClient.post()
                    .uri(this.userStatsListApi)
                    .bodyValue(longUserIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (Exception e) {
            // WebClient 호출 실패 시 로그 추가
            logger.error("Failed to call User MSA API: {}", e.getMessage());
            return List.of(); // 빈 리스트 반환하여 NullPointerException 방지
        }
    }

    private Map<String, Object> calculateStatistics(List<Map<String, Object>> demographics) {
        int maleCount = 0;
        int femaleCount = 0;
        Map<String, Integer> ageGroupCount = new HashMap<>();
        Map<String, Object> ageGroupPercentage = new HashMap<>();

        ageGroupCount.put("10대", 0);
        ageGroupCount.put("20대", 0);
        ageGroupCount.put("30대", 0);
        ageGroupCount.put("40대", 0);
        ageGroupCount.put("50대 이상", 0);

        // 1. 총 인원수 계산
        int totalPopulation = demographics.size();
        if (totalPopulation == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("gender", Map.of("male", 0, "female", 0));
            result.put("gender_percentage", Map.of("male", "0.00%", "female", "0.00%"));
            result.put("age_groups", ageGroupCount);
            ageGroupCount.forEach((key, value) -> ageGroupPercentage.put(key, "0.00%"));
            result.put("age_group_percentage", ageGroupPercentage);
            return result;
        }

        // 2. 카운트 계산
        for (Map<String, Object> user : demographics) {
            String gender = (String) user.get("gender");
            Integer age = (Integer) user.get("age");

            if ("MALE".equalsIgnoreCase(gender)) {
                maleCount++;
            } else if ("FEMALE".equalsIgnoreCase(gender)) {
                femaleCount++;
            }

            if (age != null && age > 0) {
                String ageGroup = getAgeGroup(age);
                ageGroupCount.put(ageGroup, ageGroupCount.getOrDefault(ageGroup, 0) + 1);
            }
        }

        // 3. 비율 계산 (소수점 두 자리까지)
        String malePercentage = df.format(((double) maleCount / totalPopulation) * 100) + "%";
        String femalePercentage = df.format(((double) femaleCount / totalPopulation) * 100) + "%";

        ageGroupCount.forEach((key, value) -> {
            String percentage = df.format(((double) value / totalPopulation) * 100) + "%";
            ageGroupPercentage.put(key, percentage);
        });

        // 4. 최종 결과 Map 구성
        Map<String, Object> result = new HashMap<>();
        result.put("total_population", totalPopulation);
        result.put("gender_count", Map.of("male", maleCount, "female", femaleCount));
        result.put("gender_percentage", Map.of("male", malePercentage, "female", femalePercentage));
        result.put("age_group_count", ageGroupCount);
        result.put("age_group_percentage", ageGroupPercentage);
        return result;
    }

    private String getAgeGroup(int age) {
        if (age < 20) return "10대";
        if (age < 30) return "20대";
        if (age < 40) return "30대";
        if (age < 50) return "40대";
        return "50대 이상";
    }
}