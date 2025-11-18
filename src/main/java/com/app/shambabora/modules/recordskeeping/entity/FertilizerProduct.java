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
@Table(name = "fertilizer_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FertilizerProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_profile_id", nullable = false)
    private Long farmerProfileId;

    @Column(nullable = false)
    private String productName; // e.g., "DAP", "CAN", "NPK 17:17:17"

    @Column(nullable = false)
    private String productType; // ORGANIC, SYNTHETIC, MIXED

    // Composition
    private Integer nitrogenPercent; // N%
    private Integer phosphorusPercent; // P%
    private Integer potassiumPercent; // K%
    
    private String cropTypes; // Which crops this is suitable for (comma-separated)

    private String supplier;
    private String sourceLocation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerUnit; // Price per kg/bag

    @Column(nullable = false)
    private String unit; // kg, bag, liter

    @Column(nullable = false)
    private LocalDate purchaseDate;

    // Application info
    private Integer recommendedApplicationRate; // kg/acre or units/acre
    private String applicationTiming; // Pre-planting, weeks after planting, etc.
    private Integer shelfLifeMonths; // How long can store

    // Farmer feedback
    private Integer effectivenessRating; // 1-5 stars
    private String farmerNotes; // Observations about this fertilizer
    private Integer timesUsed; // How many seasons
    private BigDecimal averageYieldIncrease; // Approximate yield increase %

    // Cost tracking
    private Boolean isCostEffective; // Based on farmer experience
    private BigDecimal costPerKgYield; // Cost relative to yield improvement

    private Boolean isActive;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (isActive == null) {
            isActive = true;
        }
        if (timesUsed == null) {
            timesUsed = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
