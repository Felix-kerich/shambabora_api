package com.app.shambabora.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WeatherForecastResponse {
    private String locationName;
    private Double temperature;
    private Integer humidity;
    private String weatherDescription;
    private Double windSpeed;
    private LocalDateTime forecastDateTime;
} 