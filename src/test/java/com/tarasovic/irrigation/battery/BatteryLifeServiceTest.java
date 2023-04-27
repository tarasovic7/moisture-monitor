package com.tarasovic.irrigation.battery;

import com.tarasovic.irrigation.measurement.PowerOutage;
import com.tarasovic.irrigation.measurement.PowerOutageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BatteryLifeServiceTest {

    @Autowired
    private BatteryLifeService batteryLifeService;

    @MockBean
    private PowerOutageService powerOutageService;

    @Test
    public void givenPowerOutageAndMeasurements_whenFindingBatteryLives_thenCorrectBatteryLifeFound() {
        Instant startOfOutage = now();
        Instant endOfOutage = startOfOutage.plus(182, MINUTES);
        when(powerOutageService.findPowerOutages()).thenReturn(List.of(new PowerOutage(startOfOutage, endOfOutage)));
        Instant startOfMeasurements = startOfOutage.minus(30, MINUTES);
        when(powerOutageService.getStartOfMeasurements()).thenReturn(startOfMeasurements);

        List<BatteryLife> batteryLives = batteryLifeService.findBatteryLives();

        assertThat(batteryLives).hasSize(1);
        assertThat(batteryLives.get(0).getDiedAt()).isEqualTo(startOfOutage);
        assertThat(batteryLives.get(0).getDurability().toMinutes()).isEqualTo(30);
    }

    @Test
    public void givenPowerOutageAndMeasurements2_whenFindingBatteryLives_thenCorrectBatteryLifeFound() {
        Instant now = now();
        int durationOfSecondRunInMinutes = 30;
        Instant secondBatteryDeath = now.plus(durationOfSecondRunInMinutes, MINUTES);
        when(powerOutageService.findPowerOutages()).thenReturn(List.of(
                new PowerOutage(now.minus(181, MINUTES), now),
                new PowerOutage(secondBatteryDeath, secondBatteryDeath.plus(181, MINUTES))));
        Instant startOfMeasurements = now.minus(190, MINUTES);
        when(powerOutageService.getStartOfMeasurements()).thenReturn(startOfMeasurements);

        List<BatteryLife> batteryLives = batteryLifeService.findBatteryLives();

        assertThat(batteryLives).hasSize(2);
        assertThat(batteryLives.get(1).getDiedAt()).isEqualTo(secondBatteryDeath);
        assertThat(batteryLives.get(1).getDurability().toMinutes()).isEqualTo(durationOfSecondRunInMinutes);
    }

}