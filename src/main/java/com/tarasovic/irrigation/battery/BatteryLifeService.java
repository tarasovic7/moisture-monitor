package com.tarasovic.irrigation.battery;

import com.tarasovic.irrigation.measurement.PowerOutage;
import com.tarasovic.irrigation.measurement.PowerOutageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class BatteryLifeService {

    private PowerOutageService powerOutageService;

    public List<BatteryLife> findBatteryLives() {
        List<PowerOutage> powerOutages = powerOutageService.findPowerOutages();
        Instant batteryStartedAt = powerOutageService.getStartOfMeasurements();
        List<BatteryLife> batteryLives = new ArrayList<>();
        for (PowerOutage powerOutage : powerOutages) {
            Instant batteryDiedAt = powerOutage.getStartTime();
            Duration durability = Duration.between(batteryStartedAt, batteryDiedAt);
            batteryLives.add(new BatteryLife(batteryDiedAt, durability));
            batteryStartedAt = powerOutage.getEndTime();
        }
        return batteryLives;
    }
}
