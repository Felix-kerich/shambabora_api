package com.app.shambabora.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseDetectionRequest {
    private Long reportId;
    private String imageUrl;
    private String cropType;
}
