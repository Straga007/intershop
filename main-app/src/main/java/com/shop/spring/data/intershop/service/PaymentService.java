package com.shop.spring.data.intershop.service;

import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<Double> checkBalance();
}