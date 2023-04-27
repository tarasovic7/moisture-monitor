package com.tarasovic.irrigation.measurement;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final MeasurementMapper measurementMapper;

    public List<MeasurementDTO> findAll() {
        return measurementRepository.findAll().stream().map(measurementMapper::measurementToMeasurementDTO).toList();
    }

    public List<MeasurementDTO> findFirst300OrderByTimeAtDesc(String plant) {
        return measurementRepository.findTopNByOrderByTimeDesc(plant, 300).stream().map(measurementMapper::measurementToMeasurementDTO).toList();
    }

    public MeasurementDTO create(MeasurementDTO measurementDTO) {
        Measurement measurement = measurementMapper.measurementDTOToMeasurement(measurementDTO);
        measurement.setTime(Instant.now());
        measurement = measurementRepository.save(measurement);
        return measurementMapper.measurementToMeasurementDTO(measurement);
    }

    public MeasurementDTO findById(Long id) {
        return measurementRepository.findById(id)
                .map(measurementMapper::measurementToMeasurementDTO)
                .orElseThrow(() -> new EntityNotFoundException(Long.toString(id)));
    }

    public MeasurementDTO removeById(Long id) {
        Optional<Measurement> measurement = measurementRepository.findById(id);
        measurement.ifPresent(measurementRepository::delete);
        return measurement.map(measurementMapper::measurementToMeasurementDTO).orElse(null);
    }

    public Double getLatestMoisture(String plant) {
        Optional<Measurement> measurements = measurementRepository.findLatestMeasurement(plant);
        return measurements.map(Measurement::getMoisture).orElse(0.0);
    }

}
