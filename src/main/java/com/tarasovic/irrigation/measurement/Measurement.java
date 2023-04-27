package com.tarasovic.irrigation.measurement;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    double moisture;

    int adc;

    Instant time;

    String plant;

    static Measurement measurement(double moisture, int adc, String plant, Instant time) {
        Measurement measurement = new Measurement();
        measurement.setMoisture(moisture);
        measurement.setTime(time);
        measurement.setAdc(adc);
        measurement.setPlant(plant);
        return measurement;
    }
}