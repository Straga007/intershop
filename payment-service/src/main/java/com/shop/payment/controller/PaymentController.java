package com.shop.payment.controller;

import com.shop.payment.api.BalanceApi;
import com.shop.payment.api.PaymentApi;
import com.shop.payment.model.BalanceResponse;
import com.shop.payment.model.PaymentRequest;
import com.shop.payment.model.PaymentResponse;
import com.shop.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController implements BalanceApi, PaymentApi {
    
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance()
                .map(balance -> {
                    BalanceResponse response = new BalanceResponse();
                    response.setBalance(balance);
                    return ResponseEntity.ok(response);
                });
    }
    
    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.flatMap(request -> 
            paymentService.processPayment(request.getAmount())
                .map(result -> {
                    PaymentResponse response = new PaymentResponse();
                    response.setSuccess(result.isSuccess());
                    response.setMessage(result.getMessage());
                    response.setRemainingBalance(result.getRemainingBalance());
                    return ResponseEntity.ok(response);
                })
        );
    }
}