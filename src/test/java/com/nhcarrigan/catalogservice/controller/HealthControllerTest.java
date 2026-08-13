package com.nhcarrigan.catalogservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional

public class HealthControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheckReturnServiceStatus() throws Exception{
        mockMvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("UP")));
    }
}