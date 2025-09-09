package com.app.shambabora.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class WeatherDailyForecastResponse {
    private String locationName;
    private String country;
    private String timezone;
    private List<DailyForecast> dailyForecasts;

    @Data
    public static class DailyForecast {
        private LocalDate date;
        private Double tempDay;
        private Double tempMin;
        private Double tempMax;
        private Double tempNight;
        private Double tempEve;
        private Double tempMorn;
        private Double feelsLikeDay;
        private Double feelsLikeNight;
        private Double feelsLikeEve;
        private Double feelsLikeMorn;
        private Integer humidity;
        private Integer pressure;
        private Double windSpeed;
        private Integer windDeg;
        private Double windGust;
        private String weatherMain;
        private String weatherDescription;
        private String weatherIcon;
        private Double rain;
        private Double snow;
        private Integer clouds;
        private Double pop;
        private Long sunrise;
        private Long sunset;
    }
} 