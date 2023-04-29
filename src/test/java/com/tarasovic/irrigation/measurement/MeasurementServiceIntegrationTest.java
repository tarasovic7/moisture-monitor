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
import java.util.stream.IntStream;

import static com.tarasovic.irrigation.measurement.Measurement.measurement;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MeasurementServiceIntegrationTest {

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private MeasurementMapper measurementMapper;

    private MeasurementService measurementService;

    @BeforeEach
    public void setUp() {
        measurementService = new MeasurementService(measurementRepository, measurementMapper);
    }

    @AfterEach
    public void tearDown() {
        measurementRepository.deleteAll();
    }

    @Test
    public void givenNoFilters_whenFindingAllMeasurements_thenReturnListOfAllMeasurements() {
        // Given
        List<Measurement> measurementList = List.of(
                measurement(45.0, 1000, "basil", now().minus(2, DAYS)),
                measurement(47.0, 2000, "parsley", now().minus(1, DAYS)),
                measurement(50.0, 3000, "mint", now().minus(3, DAYS))
        );
        measurementRepository.saveAll(measurementList);

        // When
        List<MeasurementDTO> result = measurementService.findAll();

        // Then
        assertThat(result).hasSize(measurementList.size());
        assertThat(result).extracting(MeasurementDTO::getMoisture)
                .containsExactlyInAnyOrder(45.0, 47.0, 50.0);
    }


    @Test
    public void givenPlantName_whenFindingTop300Measurements_thenReturnListOfMostRecent300Measurements() {
        // Given
        String plantName = "basil";
        List<Measurement> measurementList = IntStream.range(1, 500)
                .mapToObj(i -> measurement(40.0, 1000 + i, plantName, now().minus(i, MINUTES)))
                .toList();

        measurementRepository.saveAll(measurementList);

        // When
        List<MeasurementDTO> result = measurementService.findFirst300OrderByTimeAtDesc(plantName);

        // Then
        assertThat(result).hasSize(300);
        assertThat(result).extracting(MeasurementDTO::getAdc)
                .containsExactlyElementsOf(measurementList.subList(0, 300).stream().map(Measurement::getAdc).toList());
    }

    @Test
    public void givenNewMeasurement_whenCreatingMeasurement_thenReturnCreatedMeasurement() {
        // Given
        Instant beginningOfTest = now();
        MeasurementDTO input = new MeasurementDTO(null, 50.0, 1000, beginningOfTest, "basil");

        // When
        MeasurementDTO result = measurementService.create(input);

        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getMoisture()).isEqualTo(input.getMoisture());
        assertThat(result.getAdc()).isEqualTo(input.getAdc());
        assertThat(result.getTime()).isAfter(input.getTime());
        assertThat(result.getPlant()).isEqualTo(input.getPlant());
    }


    @Test
    public void givenNewMeasurement2_whenCreatingMeasurement_thenReturnCreatedMeasurement() {
        // Given measurement with id
        // throw exception
    }

    @Test
    public void givenExistingMeasurement_whenFindingById_thenReturnFoundMeasurement() {
        // Given
        Measurement measurement = measurement(50.0, 1000, "basil", now().minus(1, HOURS).truncatedTo(SECONDS));
        measurement = measurementRepository.save(measurement);

        // When
        MeasurementDTO result = measurementService.findById(measurement.getId());

        // Then
        assertThat(result.getId()).isEqualTo(measurement.getId());
        assertThat(result.getMoisture()).isEqualTo(measurement.getMoisture());
        assertThat(result.getPlant()).isEqualTo(measurement.getPlant());
        assertThat(result.getTime()).isEqualTo(measurement.getTime());
        assertThat(result.getAdc()).isEqualTo(measurement.getAdc());
    }

    @Test
    public void givenExistingMeasurement_whenRemovingById_thenReturnRemovedMeasurement() {
        // Given
        Measurement measurement = measurement(50.0, 1000, "basil", now().minus(1, HOURS));
        measurement = measurementRepository.save(measurement);

        // When
        MeasurementDTO result = measurementService.removeById(measurement.getId());

        // Then
        assertThat(result.getId()).isEqualTo(measurement.getId());
    }

    @Test
    public void givenFakeId_whenRemovingById_thenReturnNull() {
        // Given
        Measurement measurement = measurement(50.0, 1000, "basil", now().minus(1, HOURS));
        measurementRepository.save(measurement);
        long fakeId = 1000;

        // When
        MeasurementDTO result = measurementService.removeById(fakeId);

        assertThat(result).isNull();
    }

    @Test
    public void givenPlantName_whenGettingLatestMoisture_thenReturnLatestMoistureValue() {
        // Given
        String plantName = "basil";
        double latestValue = 40.0;
        measurementRepository.save(measurement(30.0, 3333, plantName, now()));
        measurementRepository.save(measurement(latestValue, 4444, plantName, now().plus(30, MINUTES)));

        // When
        Double result = measurementService.getLatestMoisture(plantName);

        // Then
        assertThat(result).isEqualTo(latestValue);
    }

    @Test
    public void givenNoMeasurement_whenGettingLatestMoisture_thenReturnLatestMoistureValue() {
        // Given
        // Nothing
        // When
        Double result = measurementService.getLatestMoisture("basil");

        // Then
        assertThat(result).isEqualTo(0.0);
    }

}