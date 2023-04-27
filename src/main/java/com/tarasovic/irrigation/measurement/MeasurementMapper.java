package com.tarasovic.irrigation.measurement;

import org.mapstruct.Mapper;

@Mapper
interface MeasurementMapper {

    MeasurementDTO measurementToMeasurementDTO(Measurement measurement);

    Measurement measurementDTOToMeasurement(MeasurementDTO measurementDTO);
}
