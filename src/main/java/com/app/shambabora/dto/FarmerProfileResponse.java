package com.app.shambabora.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FarmerProfileResponse {
    private Long id;
    private Long userId;
    private String farmName;
    private String county;
    private String location;
    private Double farmSize;
    private String farmDescription;
    private String alternatePhone;
    private String postalAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 