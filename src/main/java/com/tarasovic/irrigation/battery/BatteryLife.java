package com.tarasovic.irrigation.battery;

import lombok.Value;

import java.time.Duration;
import java.time.Instant;

@Value
public class BatteryLife {

    Instant diedAt;

    Duration durability;
}
