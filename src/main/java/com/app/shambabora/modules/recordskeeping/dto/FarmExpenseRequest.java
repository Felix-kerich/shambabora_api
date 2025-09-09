package com.app.shambabora.modules.recordskeeping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FarmExpenseRequest {
    @NotBlank
    private String cropType;

    @NotBlank
    private String category;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate expenseDate;

    private String supplier;
    private String invoiceNumber;
    private String paymentMethod;
    private String notes;
    private String growthStage;
    private Long farmActivityId;
    private Boolean isRecurring;
    private String recurringFrequency;
}
