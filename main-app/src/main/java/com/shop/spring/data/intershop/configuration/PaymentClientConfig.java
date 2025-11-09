package com.shop.spring.data.intershop.configuration;

import com.shop.main.client.api.DefaultApi;
import com.shop.main.client.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentClientConfig {
    
    @Value("${payment.service.url}")
    private String paymentServiceUrl;
    
    @Bean
    public DefaultApi paymentsApi() {
        WebClient webClient = WebClient.builder().build();
        
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentServiceUrl);
        
        return new DefaultApi(apiClient);
    }
}