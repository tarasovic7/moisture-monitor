package com.tarasovic.irrigation.measurement;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    @Query("SELECT m FROM Measurement m WHERE m.plant = ?1 ORDER BY m.time DESC LIMIT ?2")
    List<Measurement> findTopNByOrderByTimeDesc(String plant, int n);

    default Optional<Measurement> findLatestMeasurement(String plant){
        List<Measurement> measurements = findTopNByOrderByTimeDesc(plant, 1);
        if(measurements.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(measurements.get(0));
    }

    List<Measurement> findAll();

    List<Measurement> findByPlantOrderByTimeAsc(String plant);

    Measurement findFirstByPlantOrderByTimeAsc(String plant);

}
