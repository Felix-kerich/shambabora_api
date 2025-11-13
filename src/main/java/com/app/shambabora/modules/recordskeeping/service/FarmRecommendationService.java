package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.common.exception.BadRequestException;
import com.app.shambabora.dto.WeatherDailyForecastResponse;
import com.app.shambabora.entity.FarmerProfile;
import com.app.shambabora.modules.recordskeeping.dto.FarmAnalyticsRequest;
import com.app.shambabora.modules.recordskeeping.dto.FarmAnalyticsResponse;
import com.app.shambabora.modules.recordskeeping.dto.FarmRecommendationResponse;
import com.app.shambabora.modules.recordskeeping.entity.YieldRecord;
import com.app.shambabora.modules.recordskeeping.repository.FarmExpenseRepository;
import com.app.shambabora.modules.recordskeeping.repository.YieldRecordRepository;
import com.app.shambabora.repository.FarmerProfileRepository;
import com.app.shambabora.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmRecommendationService {

    private final FarmAnalyticsService farmAnalyticsService;
    private final YieldRecordRepository yieldRecordRepository;
    private final FarmExpenseRepository farmExpenseRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final WeatherService weatherService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${rag.service.base-url:http://localhost:8088}")
    private String ragServiceBaseUrl;

    public FarmAnalyticsResponse getAnalytics(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        FarmAnalyticsRequest request = resolveRequest(userId, cropType, startDate, endDate);
        return farmAnalyticsService.generateAnalytics(
                userId,
                request.getCropType(),
                request.getStartDate(),
                request.getEndDate()
        );
    }

    public FarmRecommendationResponse generateAdvice(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        FarmAnalyticsRequest request = resolveRequest(userId, cropType, startDate, endDate);
        FarmAnalyticsResponse analytics = farmAnalyticsService.generateAnalytics(
                userId,
                request.getCropType(),
                request.getStartDate(),
                request.getEndDate()
        );

        // Build payload expected by rag-service /advice
        Map<String, Object> context = new HashMap<>();
        context.put("crop_type", analytics.getCropType());
        context.put("period_start", analytics.getAnalysisPeriodStart() != null ? analytics.getAnalysisPeriodStart().toString() : null);
        context.put("period_end", analytics.getAnalysisPeriodEnd() != null ? analytics.getAnalysisPeriodEnd().toString() : null);
        context.put("total_expenses", toDouble(analytics.getTotalExpenses()));
        context.put("total_revenue", toDouble(analytics.getTotalRevenue()));
        context.put("net_profit", toDouble(analytics.getNetProfit()));
        context.put("profit_margin", toDouble(analytics.getProfitMargin()));
        context.put("total_yield", toDouble(analytics.getTotalYield()));
        context.put("average_yield_per_unit", toDouble(analytics.getAverageYieldPerUnit()));
        context.put("best_yield", toDouble(analytics.getBestYield()));
        context.put("worst_yield", toDouble(analytics.getWorstYield()));
        context.put("expenses_by_category", analytics.getExpensesByCategory());
        context.put("expenses_by_growth_stage", analytics.getExpensesByGrowthStage());
        context.put("prior_recommendations", analytics.getRecommendations());
        // Optional environment/context
        context.put("soil_type", request.getSoilType());
        context.put("soil_ph", request.getSoilPh());
        context.put("rainfall_mm", request.getRainfallMm());
        context.put("location", request.getLocation());
        context.put("dominant_weather", request.getDominantWeather());

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", String.valueOf(userId));
        payload.put("context", context);
        payload.put("k", 4);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(payload, headers);

        String url = ragServiceBaseUrl + "/advice";
        ResponseEntity<FarmRecommendationResponse> response =
                restTemplate.postForEntity(url, httpEntity, FarmRecommendationResponse.class);
        return response.getBody();
    }

    private Double toDouble(java.math.BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }

    private FarmAnalyticsRequest resolveRequest(Long userId, String cropType, LocalDate startDate, LocalDate endDate) {
        FarmAnalyticsRequest request = new FarmAnalyticsRequest();

        String resolvedCrop = StringUtils.hasText(cropType) ? cropType : resolvePrimaryCrop(userId);
        if (!StringUtils.hasText(resolvedCrop)) {
            throw new BadRequestException("No crop data available to generate analytics. Please record yields or expenses first.");
        }

        LocalDate resolvedEnd = endDate;
        LocalDate resolvedStart = startDate;

        List<YieldRecord> yieldRecords = yieldRecordRepository.findByFarmerProfileIdAndCropTypeOrderByHarvestDateAsc(userId, resolvedCrop);
        if (!yieldRecords.isEmpty()) {
            if (resolvedStart == null) {
                resolvedStart = yieldRecords.get(0).getHarvestDate();
            }
            if (resolvedEnd == null) {
                resolvedEnd = yieldRecords.get(yieldRecords.size() - 1).getHarvestDate();
            }
        }

        if (resolvedEnd == null) {
            resolvedEnd = LocalDate.now();
        }
        if (resolvedStart == null) {
            resolvedStart = resolvedEnd.minusMonths(12);
        }

        if (resolvedStart.isAfter(resolvedEnd)) {
            resolvedStart = resolvedEnd.minusMonths(12);
        }

        request.setCropType(resolvedCrop);
        request.setStartDate(resolvedStart);
        request.setEndDate(resolvedEnd);

        Optional<FarmerProfile> profile = farmerProfileRepository.findByUser_Id(userId);
        profile.map(FarmerProfile::getLocation)
                .filter(StringUtils::hasText)
                .ifPresent(request::setLocation);
        if (!StringUtils.hasText(request.getLocation())) {
            profile.map(FarmerProfile::getCounty)
                    .filter(StringUtils::hasText)
                    .ifPresent(request::setLocation);
        }
        enrichWithWeather(request);

        return request;
    }

    private String resolvePrimaryCrop(Long userId) {
        Optional<YieldRecord> latestYield = yieldRecordRepository.findTopByFarmerProfileIdOrderByHarvestDateDesc(userId);
        if (latestYield.isPresent() && StringUtils.hasText(latestYield.get().getCropType())) {
            return latestYield.get().getCropType();
        }

        List<String> yieldCrops = yieldRecordRepository.findDistinctCropTypesByFarmerProfileId(userId);
        if (!yieldCrops.isEmpty()) {
            return yieldCrops.get(0);
        }

        return farmExpenseRepository.findTopByFarmerProfileIdOrderByExpenseDateDesc(userId)
                .map(expense -> expense.getCropType())
                .filter(StringUtils::hasText)
                .orElseGet(() -> {
                    List<String> expenseCrops = farmExpenseRepository.findDistinctCropTypesByFarmerProfileId(userId);
                    return expenseCrops.isEmpty() ? null : expenseCrops.get(0);
                });
    }

    private void enrichWithWeather(FarmAnalyticsRequest request) {
        if (!StringUtils.hasText(request.getLocation())) {
            return;
        }
        try {
            WeatherDailyForecastResponse weather = weatherService.getCurrentWeather(request.getLocation());
            if (weather != null && weather.getDailyForecasts() != null && !weather.getDailyForecasts().isEmpty()) {
                WeatherDailyForecastResponse.DailyForecast forecast = weather.getDailyForecasts().get(0);
                if (StringUtils.hasText(forecast.getWeatherDescription())) {
                    request.setDominantWeather(forecast.getWeatherDescription());
                } else if (StringUtils.hasText(forecast.getWeatherMain())) {
                    request.setDominantWeather(forecast.getWeatherMain());
                }
                if (request.getRainfallMm() == null && forecast.getRain() != null) {
                    request.setRainfallMm(forecast.getRain());
                }
            }
        } catch (Exception ex) {
            log.warn("Unable to fetch weather data for {}: {}", request.getLocation(), ex.getMessage());
        }
    }
}

