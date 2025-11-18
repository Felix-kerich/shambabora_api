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
@Table(name = "pesticide_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesticideProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_profile_id", nullable = false)
    private Long farmerProfileId;

    @Column(nullable = false)
    private String productName; // e.g., "Sevin", "Ridomil", "Neem Oil"

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PesticideType pesticideType; // INSECTICIDE, FUNGICIDE, HERBICIDE, ACARICIDE

    @Column(nullable = false)
    private String pesticideClass; // Synthetic, Organic, Biological

    // Active ingredient
    private String activeIngredient;
    private String concentrationPercent;

    private String targetPests; // Which pests it controls (comma-separated)
    private String cropTypes; // Suitable for which crops

    private String supplier;
    private String sourceLocation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerUnit;

    @Column(nullable = false)
    private String unit; // Liter, kg, packet, etc.

    @Column(nullable = false)
    private LocalDate purchaseDate;

    // Application info
    @Column(precision = 10, scale = 2)
    private BigDecimal recommendedDosePerAcre;
    private String applicationMethod; // Spray, dust, injection, soil drench
    private Integer daysToHarvestAfterApplication; // Safety period
    private Integer effectiveDays; // How long residue active

    // Farmer experience
    private Integer effectivenessRating; // 1-5 stars
    private String pestsControlled; // Which pests actually worked on
    private String environmentalImpact; // Farmer's observations
    private String farmerNotes;
    private Integer timesUsed;

    // Health and safety
    private String toxicityLevel; // Low, Medium, High
    private String safetyPrecautions; // Important precautions
    private Boolean requiresPersonalProtection; // Whether PPE needed

    private Boolean isActive;
    private Boolean isRecommendedOrganic; // For organic farming compatibility

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

    public enum PesticideType {
        INSECTICIDE,
        FUNGICIDE,
        HERBICIDE,
        ACARICIDE,
        MOLLUSCICIDE,
        NEMATICIDE,
        RODENTICIDE,
        BIOPESTICIDE
    }
}
