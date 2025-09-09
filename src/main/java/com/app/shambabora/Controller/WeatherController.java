package com.app.shambabora.Controller;

import com.app.shambabora.dto.WeatherForecastResponse;
import com.app.shambabora.dto.WeatherDailyForecastResponse;
import com.app.shambabora.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;

    @Operation(summary = "Get current weather for a location")
    @GetMapping("/current")
    public ResponseEntity<WeatherDailyForecastResponse> getCurrentWeather(@RequestParam String location) {
        return ResponseEntity.ok(weatherService.getCurrentWeather(location));
    }

    @Operation(summary = "Get weather forecast for a location")
    @GetMapping("/forecast")
    public ResponseEntity<WeatherDailyForecastResponse> getWeatherForecast(@RequestParam String location) {
        return ResponseEntity.ok(weatherService.getWeatherForecast(location));
    }

    @Operation(summary = "Get daily weather forecast for a location (7-16 days)")
    @GetMapping("/forecast/daily")
    public ResponseEntity<WeatherDailyForecastResponse> getDailyForecast(@RequestParam String location, @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(weatherService.getDailyForecast(location, days));
    }

    @Operation(summary = "Get monthly weather statistics for a location (requires paid plan)")
    @GetMapping("/forecast/monthly")
    public ResponseEntity<WeatherDailyForecastResponse> getMonthlyStats(@RequestParam String location, @RequestParam int month) {
        return ResponseEntity.ok(weatherService.getMonthlyStats(location, month));
    }
} 