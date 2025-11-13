package com.app.shambabora.modules.recordskeeping.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FarmAnalyticsRequest {
    private String cropType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String soilType;
    private Double soilPh;
    private Double rainfallMm;
    private String location;
    private String dominantWeather;
}

