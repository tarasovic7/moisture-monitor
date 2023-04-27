package com.tarasovic.irrigation.measurement;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/measurements")
class MeasurementController {

    private final MeasurementService measurementService;

    @GetMapping
    public List<MeasurementDTO> findAll() {
        return measurementService.findAll();
    }

    @PostMapping
    public MeasurementDTO create(@RequestBody MeasurementDTO entity) {
        return measurementService.create(entity);
    }

    @GetMapping("/{id}")
    public MeasurementDTO findById(@PathVariable Long id) {
        return measurementService.findById(id);
    }

    @DeleteMapping("/{id}")
    public MeasurementDTO delete(@PathVariable Long id){
        return measurementService.removeById(id);
    }
}