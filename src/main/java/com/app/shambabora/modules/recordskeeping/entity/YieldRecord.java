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
@Table(name = "yield_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YieldRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_profile_id", nullable = false)
    private Long farmerProfileId;

    @Column(nullable = false)
    private String cropType;

    @Column(nullable = false)
    private LocalDate harvestDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal yieldAmount;

    @Column(nullable = false)
    private String unit; // kg, tons, bags, etc.

    @Column(precision = 10, scale = 2)
    private BigDecimal areaHarvested; // in acres/hectares

    @Column(precision = 10, scale = 2)
    private BigDecimal yieldPerUnit; // yield per acre/hectare

    @Column(precision = 10, scale = 2)
    private BigDecimal marketPrice; // price per unit

    @Column(precision = 10, scale = 2)
    private BigDecimal totalRevenue; // yield * market price

    private String qualityGrade; // Grade A, B, C, etc.
    private String storageLocation;
    private String buyer;
    private String notes;

    // For linking to harvest activity
    @Column(name = "farm_activity_id")
    private Long farmActivityId;

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
}
