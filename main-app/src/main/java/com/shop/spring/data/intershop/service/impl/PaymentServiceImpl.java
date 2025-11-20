package com.shop.spring.data.intershop.service.impl;

import com.shop.main.client.api.DefaultApi;
import com.shop.main.client.model.BalanceResponse;
import com.shop.spring.data.intershop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final DefaultApi paymentsApi;

    @Override
    public Mono<Double> checkBalance() {
        log.info("checkBalance");
        return paymentsApi.getBalance()
                .map(BalanceResponse::getBalance)
                .doOnNext(balance -> log.info("Получен баланс: {}", balance))
                .doOnError(error -> log.error("Ошибка при получении баланса: ", error))
                .onErrorResume(error -> {
                    log.error("Не удалось получить баланс, возвращаем значение по умолчанию 0.0: ", error);
                    return Mono.just(0.0);
                });
    }
}