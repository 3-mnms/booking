package com.mnms.booking.util;

import com.mnms.booking.dto.response.StatisticsUserResponseDTO;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsUtil {
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static StatisticsUserResponseDTO calculateStatistics(List<Map<String, Object>> demographics) {
        int maleCount = 0;
        int femaleCount = 0;
        Map<String, Integer> ageGroupCount = new HashMap<>();
        Map<String, String> ageGroupPercentage = new HashMap<>();

        ageGroupCount.put("10대", 0);
        ageGroupCount.put("20대", 0);
        ageGroupCount.put("30대", 0);
        ageGroupCount.put("40대", 0);
        ageGroupCount.put("50대 이상", 0);

        int totalPopulation = demographics.size();
        if (totalPopulation == 0) {
            Map<String, Integer> genderCount = Map.of("male", 0, "female", 0);
            Map<String, String> genderPercentage = Map.of("male", "0.00%", "female", "0.00%");
            ageGroupCount.forEach((key, value) -> ageGroupPercentage.put(key, "0.00%"));

            return new StatisticsUserResponseDTO(
                    totalPopulation,
                    genderCount,
                    genderPercentage,
                    ageGroupCount,
                    ageGroupPercentage
            );
        }

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

        String malePercentage = df.format(((double) maleCount / totalPopulation) * 100) + "%";
        String femalePercentage = df.format(((double) femaleCount / totalPopulation) * 100) + "%";
        Map<String, String> genderPercentage = Map.of("male", malePercentage, "female", femalePercentage);

        ageGroupCount.forEach((key, value) -> {
            String percentage = df.format(((double) value / totalPopulation) * 100) + "%";
            ageGroupPercentage.put(key, percentage);
        });

        return new StatisticsUserResponseDTO(
                totalPopulation,
                Map.of("male", maleCount, "female", femaleCount),
                genderPercentage,
                ageGroupCount,
                ageGroupPercentage
        );
    }

    private static String getAgeGroup(int age) {
        if (age < 20) return "10대";
        if (age < 30) return "20대";
        if (age < 40) return "30대";
        if (age < 50) return "40대";
        return "50대 이상";
    }
}