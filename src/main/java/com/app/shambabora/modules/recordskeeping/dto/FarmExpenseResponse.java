package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FarmExpenseResponse {
    private Long id;
    private Long patchId;
    private String patchName;
    private String cropType;
    private String category;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String supplier;
    private String invoiceNumber;
    private String paymentMethod;
    private String notes;
    private String growthStage;
    private Long farmActivityId;
    private Boolean isRecurring;
    private String recurringFrequency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
