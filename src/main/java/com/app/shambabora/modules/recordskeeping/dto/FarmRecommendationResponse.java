package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FarmRecommendationResponse {
    private String advice;
    private List<String> fertilizerRecommendations;
    private List<String> seedRecommendations;
    private List<String> prioritizedActions;
    private List<String> riskWarnings;
    private List<Map<String, Object>> contexts;
}

