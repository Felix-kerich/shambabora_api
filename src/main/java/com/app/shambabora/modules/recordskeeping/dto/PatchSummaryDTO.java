package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PatchSummaryDTO {
    private Long patchId;
    private String patchName;
    private Integer year;
    private String season;
    private String cropType;
    private Double area;
    private String areaUnit;

    private BigDecimal totalExpenses;
    private BigDecimal totalYield; // sum of yieldAmount
    private BigDecimal totalRevenue;
    private BigDecimal costPerKg;
    private BigDecimal profit;
    private BigDecimal profitPerKg;
    private BigDecimal roiPercentage;

    // Lists
    private List<String> activityTypes;
    private List<String> inputSummaries; // e.g., "Seed:H511 qty=10kg", "Fert:Urea 50kg"
    private List<String> expenseSummaries;
}
