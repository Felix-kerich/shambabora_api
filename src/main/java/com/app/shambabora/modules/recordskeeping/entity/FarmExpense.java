package com.app.shambabora.modules.recordskeeping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "farm_expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_profile_id", nullable = false)
    private Long farmerProfileId;

    @Column(nullable = false)
    private String cropType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    private String supplier;
    private String invoiceNumber;
    private String paymentMethod;
    private String notes;

    // For tracking expenses by growth stage
    @Enumerated(EnumType.STRING)
    private GrowthStage growthStage;

    // For linking to specific farm activity if applicable
    @Column(name = "farm_activity_id")
    private Long farmActivityId;

    // For tracking recurring expenses
    private Boolean isRecurring;
    private String recurringFrequency; // MONTHLY, WEEKLY, etc.

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ExpenseCategory {
        SEEDS,
        FERTILIZER,
        PESTICIDES,
        LABOR,
        EQUIPMENT,
        TRANSPORT,
        IRRIGATION,
        STORAGE,
        MARKETING,
        ADMINISTRATIVE,
        MAINTENANCE,
        OTHER
    }

    public enum GrowthStage {
        PRE_PLANTING,
        PLANTING,
        EARLY_GROWTH,
        VEGETATIVE,
        FLOWERING,
        FRUITING,
        MATURITY,
        HARVEST,
        POST_HARVEST,
        STORAGE
    }
}
