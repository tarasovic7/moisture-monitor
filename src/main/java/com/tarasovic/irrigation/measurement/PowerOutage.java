package com.tarasovic.irrigation.measurement;

import lombok.Value;

import java.time.Instant;

@Value
public class PowerOutage {

    Instant startTime;
    Instant endTime;
}
