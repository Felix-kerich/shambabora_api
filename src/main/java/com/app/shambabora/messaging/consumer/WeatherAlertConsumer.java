package com.app.shambabora.messaging.consumer;


//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//import static com.farmadvisor.config.RabbitMQConfig.WEATHER_ALERT_QUEUE;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class WeatherAlertConsumer {
//
//    private final WeatherAlertService weatherAlertService;
//
//    @RabbitListener(queues = WEATHER_ALERT_QUEUE)
//    public void handleWeatherAlertJob(WeatherAlertRequest request) {
//        log.info("Received weather alert generation request for county: {}", request.getCounty());
//
//        try {
//            // Here would be the actual weather API call and alert generation logic
//            // For demo purposes, we'll create a sample weather alert
//
//            WeatherAlert alert = WeatherAlert.builder()
//                    .county(request.getCounty())
//                    .alertType("Heavy Rainfall")
//                    .description("Expected heavy rainfall of 30-50mm in the next 48 hours")
//                    .startDate(LocalDate.now())
//                    .endDate(LocalDate.now().plusDays(2))
//                    .severity(WeatherAlert.AlertSeverity.MEDIUM)
//                    .recommendedActions("Ensure proper drainage in farm fields. Consider delaying any planned spraying activities.")
//                    .build();
//
//            weatherAlertService.saveWeatherAlert(alert);
//
//            log.info("Weather alert created for county: {}", request.getCounty());
//
//        } catch (Exception e) {
//            log.error("Error processing weather alert job: {}", e.getMessage(), e);
//        }
//    }
//}
