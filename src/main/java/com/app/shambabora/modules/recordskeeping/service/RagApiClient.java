package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.AiAdvisorResponseDTO;
import com.app.shambabora.modules.recordskeeping.dto.PatchRagDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.*;

/**
 * REST Client for communicating with the Maize RAG AI Service.
 * Handles all API calls to the Python FastAPI service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagApiClient {

    private final RestTemplate restTemplate;

    @Value("${rag.service.url:http://localhost:8000}")
    private String ragServiceUrl;

    @Value("${rag.service.timeout:30000}")
    private int serviceTimeout;

    /**
     * Call the RAG service /query endpoint to analyze patch data
     * and get AI-powered recommendations
     */
    public AiAdvisorResponseDTO analyzePatches(
            Long farmerId,
            List<PatchRagDataDTO> patches,
            String context
    ) {
        try {
            String query = buildAnalysisPrompt(farmerId, patches, context);

            Map<String, Object> request = new LinkedHashMap<>();
            request.put("question", query);
            request.put("farmer_id", farmerId);
            request.put("session_id", generateSessionId(farmerId));
            request.put("include_farmer_data", true);

            String endpoint = ragServiceUrl + "/api/v1/query";
            log.info("Calling RAG service at: {}", endpoint);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            @SuppressWarnings("null")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("RAG service returned successful response");
                return parseQueryResponse(response.getBody(), farmerId);
            } else {
                log.warn("RAG service returned status: {}", response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Error calling RAG service: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error in RAG analysis: {}", e.getMessage(), e);
        }

        return createFallbackResponse(farmerId);
    }

    /**
     * Stream patch data to RAG service for indexing
     */
    public boolean uploadPatchesToRag(Long farmerId, List<PatchRagDataDTO> patches) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("farmer_id", farmerId);
            payload.put("patches", patches);
            payload.put("timestamp", System.currentTimeMillis());

            String endpoint = ragServiceUrl + "/api/v1/documents/upload";
            log.info("Uploading {} patches to RAG service", patches.size());

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            @SuppressWarnings("null")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Error uploading patches to RAG: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get analytics metrics from RAG service
     */
    public Map<String, Object> getAnalytics(Long farmerId) {
        try {
            String endpoint = ragServiceUrl + "/api/v1/analytics/dashboard/summary";
            log.info("Fetching analytics from RAG service");

            HttpHeaders headers = createHeaders();
            @SuppressWarnings("null")
            HttpEntity<?> entity = new HttpEntity<>(headers);

            @SuppressWarnings("null")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody() != null ? response.getBody() : new HashMap<>();
            }

        } catch (Exception e) {
            log.error("Error fetching analytics: {}", e.getMessage(), e);
        }

        return new HashMap<>();
    }

    /**
     * Build a comprehensive analysis prompt for the AI
     * This is crucial for getting quality recommendations
     */
    private String buildAnalysisPrompt(
            Long farmerId,
            List<PatchRagDataDTO> patches,
            String additionalContext
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("# Maize Farming Analysis and Recommendations\n\n");
        prompt.append("## Farmer Profile\n");
        prompt.append("- Farmer ID: ").append(farmerId).append("\n");
        prompt.append("- Number of Patches: ").append(patches.size()).append("\n");
        prompt.append("- Analysis Date: ").append(new Date()).append("\n\n");

        if (additionalContext != null && !additionalContext.isEmpty()) {
            prompt.append("## Context\n");
            prompt.append(additionalContext).append("\n\n");
        }

        prompt.append("## Patch Data\n");
        prompt.append("---\n\n");

            // Add detailed patch information
            for (int i = 0; i < patches.size(); i++) {
                PatchRagDataDTO patch = patches.get(i);
                prompt.append(String.format("### Patch %d: %s\n", i + 1, patch.getPatchName()));
                prompt.append(String.format("- **Year:** %d | **Season:** %s\n", patch.getYear(), patch.getSeason()));
                prompt.append(String.format("- **Location:** %s\n", patch.getLocation()));
                prompt.append(String.format("- **Area:** %.2f %s\n", patch.getArea() != null ? patch.getArea() : 0, patch.getAreaUnit()));
                prompt.append(String.format("- **Crop Type:** %s\n", patch.getCropType()));
                prompt.append(String.format("- **Planting Date:** %s\n", patch.getPlantingDate()));
                prompt.append(String.format("- **Harvest Date:** %s\n\n", patch.getActualHarvestDate() != null ? patch.getActualHarvestDate() : "Not yet harvested"));

                // Performance Metrics
                prompt.append("#### Performance Metrics\n");
                prompt.append(String.format("- **Total Yield:** %.2f kg\n", patch.getTotalYield() != null ? patch.getTotalYield() : 0));
                prompt.append(String.format("- **Total Revenue:** %.2f\n", patch.getTotalRevenue() != null ? patch.getTotalRevenue() : 0));
                prompt.append(String.format("- **Total Expenses:** %.2f\n", patch.getTotalExpenses() != null ? patch.getTotalExpenses() : 0));
                prompt.append(String.format("- **Cost Per Kg:** %.2f\n", patch.getCostPerKg() != null ? patch.getCostPerKg() : 0));
                prompt.append(String.format("- **Profit:** %.2f\n", patch.getProfit() != null ? patch.getProfit() : 0));
                prompt.append(String.format("- **ROI Percentage:** %.2f%%\n\n", patch.getRoiPercentage() != null ? patch.getRoiPercentage() : 0));

                // Activities
                if (patch.getActivities() != null && !patch.getActivities().isEmpty()) {
                    prompt.append("#### Farming Activities\n");
                    for (PatchRagDataDTO.ActivityDetailDTO activity : patch.getActivities()) {
                        prompt.append(String.format("- **%s** (%s): %s\n",
                                activity.getActivityType(),
                                activity.getActivityDate(),
                                activity.getDescription()));
                        if (activity.getProductUsed() != null) {
                            prompt.append(String.format("  - Product: %s | Rate: %.2f\n",
                                    activity.getProductUsed(),
                                    activity.getApplicationRate() != null ? activity.getApplicationRate() : 0));
                        }
                    }
                    prompt.append("\n");
                }

                // Expenses breakdown
                if (patch.getExpenses() != null && !patch.getExpenses().isEmpty()) {
                    prompt.append("#### Expense Breakdown\n");
                    Map<String, Double> expensesByCategory = new HashMap<>();
                    for (PatchRagDataDTO.ExpenseDetailDTO expense : patch.getExpenses()) {
                        String category = expense.getCategory() != null ? expense.getCategory() : "Other";
                        double amount = expense.getAmount() != null ? expense.getAmount().doubleValue() : 0;
                        expensesByCategory.merge(category, amount, (oldVal, newVal) -> oldVal + newVal);
                    }
                    expensesByCategory.forEach((category, amount) ->
                            prompt.append(String.format("- **%s:** %.2f\n", category, amount)));
                    prompt.append("\n");
                }

                // Yields
                if (patch.getYields() != null && !patch.getYields().isEmpty()) {
                    prompt.append("#### Yield Records\n");
                    for (PatchRagDataDTO.YieldDetailDTO yield : patch.getYields()) {
                        prompt.append(String.format("- **%s:** %.2f %s @ %.2f per unit\n",
                                yield.getHarvestDate(),
                                yield.getYieldAmount(),
                                yield.getUnit(),
                                yield.getMarketPrice()));
                    }
                    prompt.append("\n");
                }

                prompt.append("---\n\n");
            }

        // Instructions for AI
        prompt.append("## Analysis Instructions\n\n");
        prompt.append("""
                Based on the farmer's patch data provided above, please provide comprehensive analysis and recommendations:

                ### 1. Overall Assessment
                - Summarize the farmer's farming performance
                - Identify overall trends and patterns
                - Rate the efficiency (1-10 scale)

                ### 2. Strengths
                - What is the farmer doing well?
                - Highlight successful patches or activities
                - Best performing categories (seeds, fertilizers, practices)

                ### 3. Areas for Improvement (Weaknesses)
                - Identify underperforming areas
                - Cost inefficiencies
                - Yield inconsistencies

                ### 4. Specific Recommendations
                For each recommendation, provide:
                - **Category** (SEEDS, FERTILIZER, LABOR, PESTICIDES, IRRIGATION, etc.)
                - **Specific Action** - What exactly to do
                - **Rationale** - Why this will help based on their data
                - **Expected Benefit** - Quantified improvement potential
                - **Priority** (HIGH, MEDIUM, LOW)

                ### 5. Best Practices to Replicate
                - Identify practices that worked well
                - Which patches showed best ROI?
                - Seasonal patterns to leverage

                ### 6. Crop Optimization Advice
                - Seed variety recommendations based on performance
                - Optimal planting/harvest timing
                - Soil management suggestions

                ### 7. Investment Advice
                - Where to invest for best ROI
                - Cost-saving opportunities
                - Resource allocation suggestions

                ### 8. Risk Mitigation
                - Identify risks based on patterns
                - Diversification suggestions
                - Weather resilience strategies

                Please be specific, data-driven, and actionable in all recommendations.
                """);

        return prompt.toString();
    }

    /**
     * Parse the RAG service response into AiAdvisorResponseDTO
     */
    private AiAdvisorResponseDTO parseQueryResponse(Map<String, Object> response, Long farmerId) {
        AiAdvisorResponseDTO dto = new AiAdvisorResponseDTO();
        dto.setFarmerProfileId(farmerId);

        try {
            // Extract the main response text
            String responseText = (String) response.getOrDefault("response", "");
            log.debug("RAG Response: {}", responseText);

            // Parse the response sections
            dto.setOverallAssessment(extractSection(responseText, "Overall Assessment", ""));
            dto.setStrengths(extractList(responseText, "Strengths"));
            dto.setWeaknesses(extractList(responseText, "Weaknesses"));
            dto.setRecommendations(parseRecommendations(extractSection(responseText, "Specific Recommendations", "")));
            dto.setBestPractices(parseBestPractices(extractSection(responseText, "Best Practices", "")));
            dto.setCropOptimizationAdvice(extractSection(responseText, "Crop Optimization", ""));
            dto.setInvestmentAdvice(extractSection(responseText, "Investment Advice", ""));

            log.info("Successfully parsed RAG response for farmer {}", farmerId);
            return dto;

        } catch (Exception e) {
            log.error("Error parsing RAG response: {}", e.getMessage(), e);
            return createFallbackResponse(farmerId);
        }
    }

    /**
     * Extract a text section from AI response
     */
    private String extractSection(String content, String sectionName, String defaultValue) {
        try {
            String[] markers = {
                    "### " + sectionName,
                    "## " + sectionName,
                    sectionName + ":",
                    "**" + sectionName + "**"
            };

            int startIdx = -1;
            for (String marker : markers) {
                startIdx = content.indexOf(marker);
                if (startIdx != -1) break;
            }

            if (startIdx == -1) {
                return defaultValue;
            }

            // Find the start of content after the marker
            int contentStart = content.indexOf("\n", startIdx) + 1;
            if (contentStart == 0) {
                return defaultValue;
            }

            // Find the next section marker or end
            int nextSectionIdx = Integer.MAX_VALUE;
            String[] sectionMarkers = {"###", "##", "- **"};
            for (String marker : sectionMarkers) {
                int idx = content.indexOf("\n" + marker, contentStart);
                if (idx != -1) {
                    nextSectionIdx = Math.min(nextSectionIdx, idx);
                }
            }

            int endIdx = (nextSectionIdx == Integer.MAX_VALUE) ? content.length() : nextSectionIdx;
            return content.substring(contentStart, endIdx).trim();

        } catch (Exception e) {
            log.debug("Error extracting section '{}': {}", sectionName, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Extract a bulleted list from AI response
     */
    private List<String> extractList(String content, String sectionName) {
        List<String> items = new ArrayList<>();
        try {
            String section = extractSection(content, sectionName, "");
            if (section.isEmpty()) {
                return items;
            }

            String[] lines = section.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("-") || line.startsWith("*")) {
                    // Remove bullet markers and clean
                    String item = line.replaceAll("^[-*]\\s*", "").trim();
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting list '{}': {}", sectionName, e.getMessage());
        }
        return items;
    }

    /**
     * Parse recommendations section into structured format
     */
    private List<AiAdvisorResponseDTO.RecommendationDTO> parseRecommendations(String section) {
        List<AiAdvisorResponseDTO.RecommendationDTO> recommendations = new ArrayList<>();
        try {
            String[] items = section.split("-\\s+\\*\\*");
            for (String item : items) {
                if (item.trim().isEmpty()) continue;

                AiAdvisorResponseDTO.RecommendationDTO rec = new AiAdvisorResponseDTO.RecommendationDTO();

                // Extract category (usually in bold)
                int categoryEnd = item.indexOf("**");
                if (categoryEnd > 0) {
                    rec.setCategory(item.substring(0, categoryEnd).trim());
                }

                // Extract recommendation text
                String remainder = item.substring(categoryEnd + 2);
                rec.setRecommendation(remainder.split("\n")[0].trim());

                // Try to find rationale and benefits
                if (remainder.contains("Rationale:")) {
                    rec.setRationale(extractBetween(remainder, "Rationale:", "\n"));
                }
                if (remainder.contains("Benefit:") || remainder.contains("Expected")) {
                    rec.setExpectedBenefit(extractBetween(remainder, "Benefit", "\n"));
                }

                rec.setPriority(1); // Default priority

                if (!rec.getCategory().isEmpty() || !rec.getRecommendation().isEmpty()) {
                    recommendations.add(rec);
                }
            }
        } catch (Exception e) {
            log.debug("Error parsing recommendations: {}", e.getMessage());
        }
        return recommendations;
    }

    /**
     * Parse best practices section
     */
    private List<AiAdvisorResponseDTO.BestPracticeDTO> parseBestPractices(String section) {
        List<AiAdvisorResponseDTO.BestPracticeDTO> practices = new ArrayList<>();
        try {
            String[] items = section.split("-\\s");
            for (String item : items) {
                if (item.trim().isEmpty()) continue;

                AiAdvisorResponseDTO.BestPracticeDTO practice = new AiAdvisorResponseDTO.BestPracticeDTO();
                practice.setPractice(item.split("\n")[0].trim());

                if (item.contains("Result:")) {
                    String resultStr = extractBetween(item, "Result:", "\n");
                    try {
                        // Try to extract numeric value
                        String[] parts = resultStr.split("\\s");
                        if (parts.length > 0) {
                            practice.setResults(Double.parseDouble(parts[0]));
                        }
                    } catch (NumberFormatException e) {
                        log.debug("Could not parse result value: {}", resultStr);
                    }
                }

                practices.add(practice);
            }
        } catch (Exception e) {
            log.debug("Error parsing best practices: {}", e.getMessage());
        }
        return practices;
    }

    /**
     * Helper to extract text between two markers
     */
    private String extractBetween(String content, String start, String end) {
        try {
            int startIdx = content.indexOf(start);
            if (startIdx == -1) return "";
            startIdx += start.length();

            int endIdx = content.indexOf(end, startIdx);
            if (endIdx == -1) endIdx = content.length();

            return content.substring(startIdx, endIdx).trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Create HTTP headers for RAG service calls
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("User-Agent", "ShambaBora-Backend/1.0");
        return headers;
    }

    /**
     * Get recommendations directly from the AI Advisor service
     * Optimized endpoint specifically for generating farmer recommendations
     */
    public AiAdvisorResponseDTO getAdvisorRecommendations(Long farmerId, List<PatchRagDataDTO> patches) {
        try {
            String endpoint = ragServiceUrl + "/api/v1/advisor/recommendations";
            log.info("Calling AI Advisor service at: {}", endpoint);
            log.info("Farmer ID: {}, Patches: {}", farmerId, patches.size());

            Map<String, Object> request = new LinkedHashMap<>();
            request.put("farmer_id", farmerId);
            request.put("patches", patches);
            request.put("context", buildContext(patches));
            
            log.debug("Request payload - farmer_id: {}, patches count: {}, context length: {}", 
                    farmerId, 
                    patches.size(),
                    request.get("context") != null ? ((String)request.get("context")).length() : 0);

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            @SuppressWarnings("null")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("AI Advisor service returned successful response");
                Map<String, Object> data = response.getBody();
                
                if (data != null && data.containsKey("data") && data.get("data") instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> advisorResponse = (Map<String, Object>) data.get("data");
                    return parseAdvisorResponse(advisorResponse, farmerId);
                }
            } else {
                log.warn("AI Advisor service returned status: {}", response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("Error calling AI Advisor service: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error in AI Advisor analysis: {}", e.getMessage(), e);
        }

        return createFallbackResponse(farmerId);
    }

    /**
     * Parse response from AI Advisor service endpoint
     */
    private AiAdvisorResponseDTO parseAdvisorResponse(Map<String, Object> response, Long farmerId) {
        AiAdvisorResponseDTO dto = new AiAdvisorResponseDTO();
        dto.setFarmerProfileId(farmerId);

        // Extract fields from advisor response
        dto.setOverallAssessment(getStringValue(response, "overall_assessment", ""));
        dto.setStrengths(getListValue(response, "strengths", new ArrayList<>()));
        dto.setWeaknesses(getListValue(response, "weaknesses", new ArrayList<>()));
        
        // Parse recommendations
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recs = (List<Map<String, Object>>) response.getOrDefault("recommendations", new ArrayList<>());
        List<AiAdvisorResponseDTO.RecommendationDTO> recommendations = new ArrayList<>();
        for (Map<String, Object> rec : recs) {
            AiAdvisorResponseDTO.RecommendationDTO recommendation = new AiAdvisorResponseDTO.RecommendationDTO();
            recommendation.setRecommendation(getStringValue(rec, "recommendation", ""));
            recommendation.setCategory(getStringValue(rec, "category", "GENERAL"));
            recommendation.setPriority(getIntValue(rec, "priority", 2));
            recommendation.setExpectedBenefit(getStringValue(rec, "expected_impact", ""));
            recommendation.setRationale(getStringValue(rec, "rationale", ""));
            recommendations.add(recommendation);
        }
        dto.setRecommendations(recommendations);

        // Parse best practices
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> practices = (List<Map<String, Object>>) response.getOrDefault("best_practices", new ArrayList<>());
        List<AiAdvisorResponseDTO.BestPracticeDTO> bestPractices = new ArrayList<>();
        for (Map<String, Object> practice : practices) {
            AiAdvisorResponseDTO.BestPracticeDTO bp = new AiAdvisorResponseDTO.BestPracticeDTO();
            bp.setPractice(getStringValue(practice, "practice", ""));
            bp.setReason(getStringValue(practice, "reason", ""));
            bestPractices.add(bp);
        }
        dto.setBestPractices(bestPractices);

        dto.setCropOptimizationAdvice(getStringValue(response, "crop_optimization_advice", ""));
        dto.setInvestmentAdvice(getStringValue(response, "investment_advice", ""));

        return dto;
    }

    /**
     * Build context string from patches
     */
    private String buildContext(List<PatchRagDataDTO> patches) {
        StringBuilder context = new StringBuilder();
        context.append("Farmer has ").append(patches.size()).append(" patches. ");
        
        double avgYield = patches.stream()
                .map(p -> p.getTotalYield() != null ? p.getTotalYield().doubleValue() : 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        
        double avgROI = patches.stream()
                .map(p -> p.getRoiPercentage() != null ? p.getRoiPercentage().doubleValue() : 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        
        context.append(String.format("Average yield: %.1f kg, Average ROI: %.1f%%. ", avgYield, avgROI));
        
        return context.toString();
    }

    /**
     * Extract string value from map with default
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Extract integer value from map with default
     */
    private Integer getIntValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Extract list value from map with default
     */
    private <T> List<T> getListValue(Map<String, Object> map, String key, List<T> defaultValue) {
        @SuppressWarnings("unchecked")
        List<T> value = (List<T>) map.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Generate a unique session ID for the farmer
     */
    private String generateSessionId(Long farmerId) {
        return String.format("farmer-%d-session-%d", farmerId, System.currentTimeMillis() / 1000);
    }

    /**
     * Create a fallback response when RAG service is unavailable
     */
    private AiAdvisorResponseDTO createFallbackResponse(Long farmerId) {
        AiAdvisorResponseDTO dto = new AiAdvisorResponseDTO();
        dto.setFarmerProfileId(farmerId);
        dto.setOverallAssessment(
                "We're currently analyzing your farming data. Please ensure the AI service is running and try again."
        );
        dto.setStrengths(Arrays.asList(
                "Consistent record-keeping",
                "Willing to try new techniques"
        ));
        dto.setWeaknesses(Arrays.asList(
                "Could expand documentation",
                "Limited sample size for AI analysis"
        ));
        dto.setRecommendations(new ArrayList<>());
        dto.setBestPractices(new ArrayList<>());
        dto.setCropOptimizationAdvice(
                "Continue recording all farming activities. The more data you provide, the better recommendations we can generate."
        );
        dto.setInvestmentAdvice(
                "Focus on capturing detailed records of your farming operations. This will help us provide more accurate investment guidance."
        );
        return dto;
    }
}
