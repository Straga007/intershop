package com.shop.spring.data.intershop.configuration;

import com.shop.main.client.api.DefaultApi;
import com.shop.main.client.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentClientConfig {
    
    @Bean
    public DefaultApi paymentsApi() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
        
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath("http://localhost:8081"); // Для надежности
        
        return new DefaultApi(apiClient);
    }
}