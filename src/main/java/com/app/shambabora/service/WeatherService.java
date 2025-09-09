package com.app.shambabora.service;

import com.app.shambabora.dto.WeatherForecastResponse;
import com.app.shambabora.dto.WeatherDailyForecastResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class WeatherService {
    @Value("${openweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherDailyForecastResponse getCurrentWeather(String location) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric",
                location, apiKey);
        try {
            ResponseEntity<java.util.Map> response = restTemplate.getForEntity(url, java.util.Map.class);
            return mapCurrentWeatherToDailyForecastResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage());
        }
    }

    public WeatherDailyForecastResponse getWeatherForecast(String location) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric",
                location, apiKey);
        try {
            ResponseEntity<java.util.Map> response = restTemplate.getForEntity(url, java.util.Map.class);
            return mapShortTermForecastToDailyForecastResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather forecast: " + e.getMessage());
        }
    }

    public WeatherDailyForecastResponse getDailyForecast(String location, int days) {
        // Use /data/2.5/forecast/daily endpoint (paid, or fallback to 5-day/3-hour for free)
        String url = String.format(
            "https://api.openweathermap.org/data/2.5/forecast/daily?q=%s&cnt=%d&appid=%s&units=metric",
            location, days, apiKey);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return mapDailyForecastResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch daily forecast: " + e.getMessage());
        }
    }

    public WeatherDailyForecastResponse getMonthlyStats(String location, int month) {
        // Use /aggregated/month endpoint (paid)
        String url = String.format(
            "https://history.openweathermap.org/data/2.5/aggregated/month?q=%s&month=%d&appid=%s",
            location, month, apiKey);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return mapMonthlyStatsResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch monthly stats: " + e.getMessage());
        }
    }

    private WeatherDailyForecastResponse mapCurrentWeatherToDailyForecastResponse(java.util.Map body) {
        WeatherDailyForecastResponse resp = new WeatherDailyForecastResponse();
        resp.setLocationName((String) body.get("name"));
        resp.setCountry(body.get("sys") != null ? (String) ((Map) body.get("sys")).get("country") : null);
        resp.setTimezone(body.get("timezone") != null ? body.get("timezone").toString() : null);
        WeatherDailyForecastResponse.DailyForecast f = new WeatherDailyForecastResponse.DailyForecast();
        f.setDate(java.time.Instant.ofEpochSecond(((Number) body.get("dt")).longValue()).atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        Map main = (Map) body.get("main");
        if (main != null) {
            f.setTempDay(getDouble(main, "temp"));
            f.setTempMin(getDouble(main, "temp_min"));
            f.setTempMax(getDouble(main, "temp_max"));
            f.setHumidity(getInt(main, "humidity"));
            f.setPressure(getInt(main, "pressure"));
        }
        Map wind = (Map) body.get("wind");
        if (wind != null) {
            f.setWindSpeed(getDouble(wind, "speed"));
            f.setWindDeg(getInt(wind, "deg"));
            f.setWindGust(getDouble(wind, "gust"));
        }
        List weatherList = (List) body.get("weather");
        if (weatherList != null && !weatherList.isEmpty()) {
            Map weather = (Map) weatherList.get(0);
            f.setWeatherMain((String) weather.get("main"));
            f.setWeatherDescription((String) weather.get("description"));
            f.setWeatherIcon((String) weather.get("icon"));
        }
        f.setClouds(body.get("clouds") instanceof Map ? getInt((Map) body.get("clouds"), "all") : null);
        f.setRain(body.get("rain") instanceof Map ? getDouble((Map) body.get("rain"), "1h") : null);
        f.setSnow(body.get("snow") instanceof Map ? getDouble((Map) body.get("snow"), "1h") : null);
        f.setSunrise(body.get("sys") != null ? getLong((Map) body.get("sys"), "sunrise") : null);
        f.setSunset(body.get("sys") != null ? getLong((Map) body.get("sys"), "sunset") : null);
        List<WeatherDailyForecastResponse.DailyForecast> forecasts = new ArrayList<>();
        forecasts.add(f);
        resp.setDailyForecasts(forecasts);
        return resp;
    }

    private WeatherDailyForecastResponse mapShortTermForecastToDailyForecastResponse(java.util.Map body) {
        WeatherDailyForecastResponse resp = new WeatherDailyForecastResponse();
        Map city = (Map) body.get("city");
        resp.setLocationName(city != null ? (String) city.get("name") : null);
        resp.setCountry(city != null ? (String) city.get("country") : null);
        resp.setTimezone(city != null && city.get("timezone") != null ? city.get("timezone").toString() : null);
        List<Map> list = (List<Map>) body.get("list");
        List<WeatherDailyForecastResponse.DailyForecast> forecasts = new ArrayList<>();
        if (list != null) {
            for (Map entry : list) {
                WeatherDailyForecastResponse.DailyForecast f = new WeatherDailyForecastResponse.DailyForecast();
                f.setDate(entry.get("dt") != null ? java.time.Instant.ofEpochSecond(((Number) entry.get("dt")).longValue()).atZone(java.time.ZoneId.systemDefault()).toLocalDate() : null);
                Map main = (Map) entry.get("main");
                if (main != null) {
                    f.setTempDay(getDouble(main, "temp"));
                    f.setTempMin(getDouble(main, "temp_min"));
                    f.setTempMax(getDouble(main, "temp_max"));
                    f.setHumidity(getInt(main, "humidity"));
                    f.setPressure(getInt(main, "pressure"));
                }
                Map wind = (Map) entry.get("wind");
                if (wind != null) {
                    f.setWindSpeed(getDouble(wind, "speed"));
                    f.setWindDeg(getInt(wind, "deg"));
                    f.setWindGust(getDouble(wind, "gust"));
                }
                List weatherList = (List) entry.get("weather");
                if (weatherList != null && !weatherList.isEmpty()) {
                    Map weather = (Map) weatherList.get(0);
                    f.setWeatherMain((String) weather.get("main"));
                    f.setWeatherDescription((String) weather.get("description"));
                    f.setWeatherIcon((String) weather.get("icon"));
                }
                f.setClouds(entry.get("clouds") instanceof Map ? getInt((Map) entry.get("clouds"), "all") : null);
                f.setRain(entry.get("rain") instanceof Map ? getDouble((Map) entry.get("rain"), "3h") : null);
                f.setSnow(entry.get("snow") instanceof Map ? getDouble((Map) entry.get("snow"), "3h") : null);
                forecasts.add(f);
            }
        }
        resp.setDailyForecasts(forecasts);
        return resp;
    }

    private WeatherDailyForecastResponse mapDailyForecastResponse(Map body) {
        WeatherDailyForecastResponse resp = new WeatherDailyForecastResponse();
        Map city = (Map) body.get("city");
        resp.setLocationName(city != null ? (String) city.get("name") : null);
        resp.setCountry(city != null ? (String) city.get("country") : null);
        resp.setTimezone(city != null && city.get("timezone") != null ? city.get("timezone").toString() : null);
        List<Map> list = (List<Map>) body.get("list");
        List<WeatherDailyForecastResponse.DailyForecast> forecasts = new ArrayList<>();
        if (list != null) {
            for (Map day : list) {
                WeatherDailyForecastResponse.DailyForecast f = new WeatherDailyForecastResponse.DailyForecast();
                f.setDate(java.time.Instant.ofEpochSecond(((Number) day.get("dt")).longValue()).atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                Map temp = (Map) day.get("temp");
                if (temp != null) {
                    f.setTempDay(getDouble(temp, "day"));
                    f.setTempMin(getDouble(temp, "min"));
                    f.setTempMax(getDouble(temp, "max"));
                    f.setTempNight(getDouble(temp, "night"));
                    f.setTempEve(getDouble(temp, "eve"));
                    f.setTempMorn(getDouble(temp, "morn"));
                }
                Map feelsLike = (Map) day.get("feels_like");
                if (feelsLike != null) {
                    f.setFeelsLikeDay(getDouble(feelsLike, "day"));
                    f.setFeelsLikeNight(getDouble(feelsLike, "night"));
                    f.setFeelsLikeEve(getDouble(feelsLike, "eve"));
                    f.setFeelsLikeMorn(getDouble(feelsLike, "morn"));
                }
                f.setHumidity(getInt(day, "humidity"));
                f.setPressure(getInt(day, "pressure"));
                f.setWindSpeed(getDouble(day, "speed"));
                f.setWindDeg(getInt(day, "deg"));
                f.setWindGust(getDouble(day, "gust"));
                List weatherList = (List) day.get("weather");
                if (weatherList != null && !weatherList.isEmpty()) {
                    Map weather = (Map) weatherList.get(0);
                    f.setWeatherMain((String) weather.get("main"));
                    f.setWeatherDescription((String) weather.get("description"));
                    f.setWeatherIcon((String) weather.get("icon"));
                }
                f.setRain(getDouble(day, "rain"));
                f.setSnow(getDouble(day, "snow"));
                f.setClouds(getInt(day, "clouds"));
                f.setPop(getDouble(day, "pop"));
                f.setSunrise(getLong(day, "sunrise"));
                f.setSunset(getLong(day, "sunset"));
                forecasts.add(f);
            }
        }
        resp.setDailyForecasts(forecasts);
        return resp;
    }

    private WeatherDailyForecastResponse mapMonthlyStatsResponse(Map body) {
        // This is a simplified mapping for monthly stats, you can expand as needed
        WeatherDailyForecastResponse resp = new WeatherDailyForecastResponse();
        Map result = (Map) body.get("result");
        resp.setLocationName((String) body.get("city_id"));
        resp.setCountry(null);
        resp.setTimezone(null);
        List<WeatherDailyForecastResponse.DailyForecast> forecasts = new ArrayList<>();
        if (result != null) {
            WeatherDailyForecastResponse.DailyForecast f = new WeatherDailyForecastResponse.DailyForecast();
            f.setDate(null);
            Map temp = (Map) result.get("temp");
            if (temp != null) {
                f.setTempMin(getDouble(temp, "average_min"));
                f.setTempMax(getDouble(temp, "average_max"));
            }
            f.setHumidity(getInt((Map) result.get("humidity"), "mean"));
            f.setPressure(getInt((Map) result.get("pressure"), "mean"));
            f.setWindSpeed(getDouble((Map) result.get("wind"), "mean"));
            f.setClouds(getInt((Map) result.get("clouds"), "mean"));
            forecasts.add(f);
        }
        resp.setDailyForecasts(forecasts);
        return resp;
    }

    private Double getDouble(Map map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : null;
    }
    private Integer getInt(Map map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).intValue() : null;
    }
    private Long getLong(Map map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).longValue() : null;
    }
} 