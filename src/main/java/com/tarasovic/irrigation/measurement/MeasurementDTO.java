package com.tarasovic.irrigation.measurement;

import lombok.Value;

import java.time.Instant;

@Value
class MeasurementDTO {

    Long id;

    double moisture;

    int adc;

    Instant time;

    String plant;
}
