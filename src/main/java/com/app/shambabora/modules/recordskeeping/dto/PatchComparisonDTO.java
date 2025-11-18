package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.util.List;

@Data
public class PatchComparisonDTO {
    private List<PatchSummaryDTO> patches;
}
