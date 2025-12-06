package com.app.shambabora.modules.recordskeeping.service;

import com.app.shambabora.modules.recordskeeping.dto.AiAdvisorResponseDTO;
import com.app.shambabora.modules.recordskeeping.dto.PatchRagDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service to integrate with the RAG (Retrieval-Augmented Generation) AI service.
 * Sends patch data to RAG for analysis and retrieves personalized farmer recommendations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagIntegrationService {

    private final RagApiClient ragApiClient;
    private final MaizePatchService maizePatchService;

    @Value("${rag.service.enabled:true}")
    private Boolean ragServiceEnabled;

    /**
     * Get AI-driven recommendations for a farmer based on their patch history.
     * Sends all patches to RAG service for comprehensive analysis.
     */
    public AiAdvisorResponseDTO getAiRecommendations(Long farmerProfileId) {
        log.info("Getting AI recommendations for farmer: {}", farmerProfileId);
        
        if (!ragServiceEnabled) {
            log.warn("RAG service is disabled. Returning placeholder recommendations.");
            return createPlaceholderResponse(farmerProfileId);
        }

        try {
            // Get all patch data in RAG-optimized format
            List<PatchRagDataDTO> patches = maizePatchService.getAllPatchesForRag(farmerProfileId);

            if (patches.isEmpty()) {
                log.warn("No patches found for farmer {}. Returning placeholder response.", farmerProfileId);
                return createPlaceholderResponse(farmerProfileId);
            }

            log.info("Found {} patches for farmer {}. Sending to AI Advisor service.", patches.size(), farmerProfileId);

            // Get AI analysis from the dedicated AI Advisor endpoint
            AiAdvisorResponseDTO response = ragApiClient.getAdvisorRecommendations(farmerProfileId, patches);

            if (response != null && response.getOverallAssessment() != null) {
                log.info("Successfully retrieved recommendations for farmer {}", farmerProfileId);
                return response;
            }

        } catch (Exception e) {
            log.error("Error getting recommendations from RAG service: {}", e.getMessage(), e);
        }

        return createPlaceholderResponse(farmerProfileId);
    }

    /**
     * Get all patches in RAG-optimized format.
     */
    public List<PatchRagDataDTO> getAllPatchesData(Long farmerProfileId) {
        try {
            List<PatchRagDataDTO> patches = maizePatchService.getAllPatchesForRag(farmerProfileId);
            log.info("Retrieved {} patches for RAG analysis for farmer {}", patches.size(), farmerProfileId);
            return patches;
        } catch (Exception e) {
            log.error("Error retrieving patches for RAG: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Sync all patches with RAG service for indexing
     */
    public boolean syncPatchesToRag(Long farmerProfileId) {
        try {
            List<PatchRagDataDTO> patches = maizePatchService.getAllPatchesForRag(farmerProfileId);
            
            if (patches.isEmpty()) {
                log.info("No patches to sync for farmer {}", farmerProfileId);
                return true;
            }

            boolean success = ragApiClient.uploadPatchesToRag(farmerProfileId, patches);
            
            if (success) {
                log.info("Successfully synced {} patches to RAG service for farmer {}", patches.size(), farmerProfileId);
            } else {
                log.warn("Failed to sync patches to RAG service for farmer {}", farmerProfileId);
            }
            
            return success;

        } catch (Exception e) {
            log.error("Error syncing patches to RAG: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get analytics from RAG service
     */
    public Map<String, Object> getAnalytics(Long farmerProfileId) {
        try {
            return ragApiClient.getAnalytics(farmerProfileId);
        } catch (Exception e) {
            log.error("Error fetching analytics: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Create a placeholder response when RAG service is unavailable.
     */
    private AiAdvisorResponseDTO createPlaceholderResponse(Long farmerProfileId) {
        AiAdvisorResponseDTO dto = new AiAdvisorResponseDTO();
        dto.setFarmerProfileId(farmerProfileId);
        dto.setOverallAssessment(
                "We're currently analyzing your farming data. Please ensure the AI service is running and try again."
        );
        dto.setStrengths(Arrays.asList(
                "Consistent record-keeping",
                "Willingness to adopt new farming techniques"
        ));
        dto.setWeaknesses(Arrays.asList(
                "Could expand documentation",
                "Limited historical data for comprehensive AI analysis"
        ));
        dto.setRecommendations(new ArrayList<>());
        dto.setBestPractices(new ArrayList<>());
        dto.setCropOptimizationAdvice(
                "Continue recording all farming activities, including planting dates, inputs used, and yields. The more detailed your records, the better recommendations we can generate."
        );
        dto.setInvestmentAdvice(
                "Focus on capturing detailed records of your farming operations. Document all expenses and yields to help our AI service provide more accurate investment guidance."
        );
        return dto;
    }
}
