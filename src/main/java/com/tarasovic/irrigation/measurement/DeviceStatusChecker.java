package com.tarasovic.irrigation.measurement;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;

@Service
@AllArgsConstructor
public class DeviceStatusChecker {

    public static final int OFFLINE_TIME_THRESHOLD_IN_MINUTES = 61;
    public static final double WATTER_PUMP_OFFLINE_THRESHOLD = 47.0;


    private final MeasurementRepository measurementRepository;

    private static final String PLANT = "basil";

    public boolean isWatterPumpOffline() {
        return measurementRepository.findTopNByOrderByTimeDesc(PLANT, 3)
                .stream()
                .allMatch(measurement -> measurement.getMoisture() < WATTER_PUMP_OFFLINE_THRESHOLD);
    }

    public boolean isDeviceOffline() {
        Optional<Measurement> measurements = measurementRepository.findLatestMeasurement(PLANT);
        return measurements.map(measurement -> measurement.getTime()
                .plus(OFFLINE_TIME_THRESHOLD_IN_MINUTES, MINUTES)
                .isBefore(now())).orElse(true);
    }
}
