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
@Table(name = "input_usage_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputUsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_profile_id", nullable = false)
    private Long farmerProfileId;

    // Link to the activity where input was used
    @Column(name = "farm_activity_id")
    private Long farmActivityId;

    // Link to the patch/plot this input application belongs to
    @Column(name = "patch_id")
    private Long patchId;

    // Optional cached patch name for quick display
    private String patchName;

    // Link to the yield record resulting from this activity
    @Column(name = "yield_record_id")
    private Long yieldRecordId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InputType inputType;

    // IDs of the input products used
    @Column(name = "seed_variety_id")
    private Long seedVarietyId;

    @Column(name = "fertilizer_product_id")
    private Long fertilizerProductId;

    @Column(name = "pesticide_product_id")
    private Long pesticideProductId;

    // Usage details
    @Column(precision = 10, scale = 2)
    private BigDecimal quantityUsed;

    @Column(nullable = false)
    private String unit; // kg, liters, packets, etc.

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal costOfUsage; // How much was spent on this

    // For fertilizer/pesticide tracking
    @Column(precision = 10, scale = 2)
    private BigDecimal applicationRate; // kg/acre or liters/acre

    // Farmer's rating of this input's performance
    private Integer effectivenessRating; // 1-5 stars
    private String visibleResults; // What farmer observed

    // For tracking correlations with yield
    private Boolean contributedToHighYield; // Did this input contribute?
    private Integer estimatedYieldContributionPercent; // Farmer's estimate

    // Additional notes
    private String notes;
    private String problemsEncountered;

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

    public enum InputType {
        SEED,
        FERTILIZER,
        PESTICIDE,
        HERBICIDE,
        FUNGICIDE,
        INSECTICIDE,
        SOIL_AMENDMENT,
        BIOFERTILIZER,
        OTHER
    }
}
