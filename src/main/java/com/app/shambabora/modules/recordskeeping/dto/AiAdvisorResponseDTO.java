package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO from RAG AI Advisor service.
 * Contains personalized recommendations based on patch history analysis.
 */
@Data
public class AiAdvisorResponseDTO {
    private Long farmerProfileId;
    private String overallAssessment; // High-level summary of farmer's performance
    private List<String> strengths; // What the farmer is doing well
    private List<String> weaknesses; // Areas that need improvement
    private List<RecommendationDTO> recommendations; // Specific actionable recommendations
    private List<BestPracticeDTO> bestPractices; // Patterns from successful patches
    private String cropOptimizationAdvice; // General advice on crop optimization
    private String investmentAdvice; // Where to invest for better ROI

    @Data
    public static class RecommendationDTO {
        private String category; // FERTILIZER, SEED, LABOUR, PESTICIDE, IRRIGATION, etc.
        private String recommendation; // The actual recommendation
        private String rationale; // Why this is recommended
        private String expectedBenefit; // What benefit can be expected
        private String evidence; // Data-backed evidence from farmer's own patches
        private Integer priority; // 1 = high, 2 = medium, 3 = low
    }

    @Data
    public static class BestPracticeDTO {
        private String practice; // Description of the best practice
        private String patchExample; // Which patch did this work well in?
        private Double results; // The result achieved (e.g., yield, ROI)
        private String reason; // Why it worked
    }
}
