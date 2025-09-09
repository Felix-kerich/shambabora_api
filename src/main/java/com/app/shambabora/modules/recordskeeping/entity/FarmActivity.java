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
@Table(name = "farm_activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_profile_id", nullable = false)
    private Long farmerProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private String cropType;

    private LocalDate activityDate;
    private String description;
    private Double areaSize;
    private String units;

    // For harvest/yield recording
    private Double yield;
    private BigDecimal cost;

    // Additional fields for specific activities
    private String productUsed;  // For spraying, fertilizing
    private Double applicationRate;
    
    // Enhanced fields for better tracking
    private String weatherConditions;
    private String soilConditions;
    private String notes;
    private String location; // Specific field/plot location
    
    // For tracking labor and equipment
    private Integer laborHours;
    private String equipmentUsed;
    private BigDecimal laborCost;
    private BigDecimal equipmentCost;

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

    public enum ActivityType {
        LAND_PREPARATION,
        PLANTING,
        SEEDING,
        TRANSPLANTING,
        IRRIGATION,
        FERTILIZING,
        TOP_DRESSING,
        SPRAYING,
        PEST_CONTROL,
        DISEASE_CONTROL,
        WEEDING,
        PRUNING,
        THINNING,
        HARVESTING,
        YIELD_RECORDING,
        STORAGE,
        TRANSPORT,
        MARKETING,
        MAINTENANCE,
        OTHER
    }
}
