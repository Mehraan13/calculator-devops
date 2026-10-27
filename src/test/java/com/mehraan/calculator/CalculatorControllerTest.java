package com.mehraan.calculator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addReturnsResultJson() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.result").value(5.0));
    }

    @Test
    void divideByZeroReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/divide").param("a", "4").param("b", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed"));
    }

    @Test
    void missingParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonNumericParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add").param("a", "x").param("b", "3"))
                .andExpect(status().isBadRequest());
    }
}
