package com.shop.spring.data.intershop.integration;

import com.shop.spring.data.intershop.IntershopApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = IntershopApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties",
    properties = {
        "spring.sql.init.mode=never",
        "spring.r2dbc.initialization-mode=never"
    })
public class WebConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testStaticResourceAccessIsBlockedViaStaticPath() throws Exception {
        mockMvc.perform(get("/static/css/main.css"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testStaticResourceAccessIsAllowedViaRootPath() throws Exception {
        mockMvc.perform(get("/css/main.css"))
                .andExpect(status().isOk());
    }
}