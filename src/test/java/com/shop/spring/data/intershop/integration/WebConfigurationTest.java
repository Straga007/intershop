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
@TestPropertySource(locations = "classpath:application-test.properties")
public class WebConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testStaticResourceAccessIsNotTrue() throws Exception {
        mockMvc.perform(get("/static/css/main.css"))
                .andExpect(status().isNotFound());
    }

}
