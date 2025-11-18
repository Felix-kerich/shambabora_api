package com.app.shambabora.modules.recordskeeping.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maize_patches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaizePatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmer_profile_id", nullable = false)
    private Long farmerProfileId;

    @Column(nullable = false)
    private Integer year; // e.g., 2025

    @Column(nullable = false)
    private String season; // e.g., LONG_RAIN, SHORT_RAIN, DRY

    @Column(nullable = false)
    private String name; // Friendly name for the patch (e.g., "Block A - 2025")

    @Column(nullable = false)
    private String cropType; // should be 'Maize' for our use-case

    private Double area; // area size in hectares/ acres depending on unit
    private String areaUnit;

    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;
    private LocalDate actualHarvestDate;

    private String location; // free text/location id
    private String notes;

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
