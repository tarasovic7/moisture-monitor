package com.tarasovic.irrigation.measurement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;

import static com.tarasovic.irrigation.measurement.Measurement.measurement;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PowerOutageServiceTest {

    @Autowired
    private MeasurementRepository measurementRepository;

    private PowerOutageService powerOutageService;

    @BeforeEach
    public void setUp() {
        powerOutageService = new PowerOutageService(measurementRepository);
    }

    @AfterEach
    public void tearDown(){
        measurementRepository.deleteAll();
    }
    @Test
    public void givenMeasurementsWithPowerOutages_whenFindingPowerOutages_thenCorrectPowerOutageFound() {
        Measurement measurement1 = measurement(45.0, 44444, "basil", now());
        measurementRepository.save(measurement1);
        Measurement measurement2 = measurement(55.0, 55555, "basil", now().plus(30, MINUTES).truncatedTo(SECONDS));
        measurementRepository.save(measurement2);
        Measurement measurement3 = measurement(55.0, 55555, "basil", now().plus(30 + 182, MINUTES).truncatedTo(SECONDS));
        measurementRepository.save(measurement3);
        measurementRepository.save(measurement(65.0, 66666, "basil", now().plus(30 + 182 + 30, MINUTES)));

        List<PowerOutage> powerOutages = powerOutageService.findPowerOutages();

        assertThat(powerOutages).hasSize(1);
        assertThat(powerOutages.get(0).getStartTime()).isEqualTo(measurement2.getTime());
        assertThat(powerOutages.get(0).getEndTime()).isEqualTo(measurement3.getTime());
    }

    @Test
    public void givenMeasurementsForPlantBasilWithTwoOutages_whenFindingPowerOutages_thenCorrectPowerOutageFound() {
        Instant now = now();

        measurementRepository.save(measurement(45.0, 44444, "basil", now));
        measurementRepository.save(measurement(55.0, 55555, "basil", now.plus(30, MINUTES)));
        measurementRepository.save(measurement(55.0, 55555, "basil", now.plus(30 + 182, MINUTES)));
        Measurement measurement1 = measurement(65.0, 66655, "basil", now.plus(30 + 182 + 45, MINUTES).truncatedTo(SECONDS));
        measurementRepository.save(measurement1);
        Measurement measurement2 = measurement(65.0, 66666, "basil", now.plus(30 + 182 + 45 + 182, MINUTES).truncatedTo(SECONDS));
        measurementRepository.save(measurement2);

        List<PowerOutage> batteryLifeTimes = powerOutageService.findPowerOutages();

        assertThat(batteryLifeTimes).hasSize(2);
        assertThat(batteryLifeTimes.get(1).getStartTime()).isEqualTo(measurement1.getTime());
        assertThat(batteryLifeTimes.get(1).getEndTime()).isEqualTo(measurement2.getTime());
    }

    @Test
    public void givenMeasurements_whenGettingStartOfMeasurements_thenCorrectStartReturned() {
        measurementRepository.save(measurement(40.0, 44444, "basil", now().plus(30, MINUTES)));
        Measurement measurement = measurement(33.0, 33333, "basil", now().truncatedTo(SECONDS));
        measurementRepository.save(measurement);

        Instant startOfMeasurements = powerOutageService.getStartOfMeasurements();

        assertThat(startOfMeasurements).isEqualTo(measurement.getTime());
    }

}