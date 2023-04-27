package com.tarasovic.irrigation.measurement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenNoAuthorization_whenGettingMeasurements_thenReturnedStatusUnautohrized() throws Exception {
        mockMvc.perform(get("/api/mesurements"))
                .andExpect(status().isUnauthorized());
    }
}