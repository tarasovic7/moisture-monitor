package com.tarasovic.irrigation.measurement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static com.tarasovic.irrigation.measurement.Measurement.measurement;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DeviceStatusCheckerTest {

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private DeviceStatusChecker deviceStatusChecker;

    @Test
    public void givenMoistureMeasurementsBelowThreshold_whenCheckingWaterPumpStatus_thenWaterPumpIsOffline() {
        measurementRepository.save(measurement(45.0, 1000, "basil", now().minus(62, MINUTES)));
        measurementRepository.save(measurement(46.0, 2000, "basil", now().minus(63, MINUTES)));
        measurementRepository.save(measurement(46.9, 3000, "basil", now().minus(64, MINUTES)));

        boolean isWatterPumpOffline = deviceStatusChecker.isWatterPumpOffline();

        assertThat(isWatterPumpOffline).isTrue();
    }

    @Test
    public void givenRecentMeasurements_whenCheckingDeviceStatus_thenDeviceIsOffline(){
        Instant now = Instant.now();
        measurementRepository.save(measurement(45.0, 1000, "basil", now.minus(120, MINUTES)));
        measurementRepository.save(measurement(46.0, 2000, "basil", now.minus(80, MINUTES)));

        boolean isDeviceOffline = deviceStatusChecker.isDeviceOffline();

        assertThat(isDeviceOffline).isTrue();
    }

}