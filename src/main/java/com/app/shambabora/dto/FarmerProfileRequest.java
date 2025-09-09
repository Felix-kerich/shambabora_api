package com.app.shambabora.dto;

import lombok.Data;

@Data
public class FarmerProfileRequest {
    private String farmName;
    private String county;
    private String location;
    private Double farmSize;
    private String farmDescription;
    private String alternatePhone;
    private String postalAddress;
} 