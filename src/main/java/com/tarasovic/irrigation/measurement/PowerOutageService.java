package com.tarasovic.irrigation.measurement;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
@AllArgsConstructor
public class PowerOutageService {

    private static final String PLANT = "basil";

    private MeasurementRepository measurementRepository;

    public static final int MINIMAL_POWER_OUTAGE_DURATION_IN_MINUTES = 181;

    public List<PowerOutage> findPowerOutages() {
        List<Measurement> measurements = measurementRepository.findByPlantOrderByTimeAsc(PLANT);
        List<PowerOutage> powerOutageTimes = new ArrayList<>();
        for (int i = 1; i < measurements.size(); i++) {
            Measurement previousMeasurement = measurements.get(i - 1);
            Measurement currentMeasurement = measurements.get(i);
            if (previousMeasurement.getTime().isBefore(currentMeasurement.getTime().minus(MINIMAL_POWER_OUTAGE_DURATION_IN_MINUTES, MINUTES))) {
                powerOutageTimes.add(new PowerOutage(previousMeasurement.getTime(), currentMeasurement.getTime()));
            }
        }
        return powerOutageTimes;
    }

    public Instant getStartOfMeasurements(){
        Measurement firstMeasurement = measurementRepository.findFirstByPlantOrderByTimeAsc(PLANT);
        return firstMeasurement.getTime();
    }
}
