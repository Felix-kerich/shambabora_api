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
@Table(name = "seed_varieties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeedVariety {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_profile_id", nullable = false)
    private Long farmerProfileId;

    @Column(nullable = false)
    private String cropType; // Maize, Beans, Tomato, etc.

    @Column(nullable = false)
    private String varietyName; // Example: "H511", "KH500"

    private String supplier; // Where farmer bought from
    private String sourceLocation; // Where seed originated from

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerUnit; // Price per kg/bag

    @Column(precision = 10, scale = 2)
    private BigDecimal quantityBought;

    @Column(nullable = false)
    private String unit; // kg, bag, packet

    @Column(nullable = false)
    private LocalDate purchaseDate;

    // Agronomic characteristics
    private Integer daysToMaturity; // How many days to harvest
    private String soilPreference; // Sandy, loamy, clay, mixed
    private String waterRequirement; // Low, medium, high
    private String diseaseResistance; // Text describing resistant diseases
    private String pestResistance; // Text describing pest resistance
    private String climateAdaptation; // Dry, wet, moderate

    // Farmer's feedback
    private Integer yieldRating; // 1-5 stars based on farmer experience
    private Integer easinessRating; // 1-5 how easy to grow
    private String farmerNotes; // Farmer's notes about this variety
    
    // Tracking usage
    private Boolean isActive; // Still using this variety?
    private Integer timesUsed; // How many seasons used
    private BigDecimal averageYieldPerUnit; // Average yield they got with this seed

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
