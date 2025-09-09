package com.app.shambabora.repository;

import com.app.shambabora.entity.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {
    List<WeatherAlert> findByCounty(String county);
    List<WeatherAlert> findByCountyAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String county, LocalDate currentDate, LocalDate currentDate2);
}