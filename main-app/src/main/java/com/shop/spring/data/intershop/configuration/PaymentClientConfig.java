package com.shop.spring.data.intershop.configuration;

import com.shop.main.client.ApiClient;
import com.shop.main.client.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentClientConfig {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Bean
    public DefaultApi paymentClient(WebClient webClient) {
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentServiceUrl + "/api/v1");
        return new DefaultApi(apiClient);
    }
}