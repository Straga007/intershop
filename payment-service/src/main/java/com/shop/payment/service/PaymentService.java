package com.shop.payment.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PaymentService {
    
    private double currentBalance = 100000.0; //баланс
    
    public Mono<Double> getBalance() {
        return Mono.just(currentBalance);
    }
    
    public Mono<PaymentResult> processPayment(double amount) {
        if (amount <= 0) {
            return Mono.just(new PaymentResult(false, "Invalid amount", currentBalance));
        }
        
        if (currentBalance >= amount) {
            currentBalance -= amount;
            return Mono.just(new PaymentResult(true, "Payment successful", currentBalance));
        } else {
            return Mono.just(new PaymentResult(false, "Insufficient funds", currentBalance));
        }
    }
    
    public static class PaymentResult {
        private final boolean success;
        private final String message;
        private final double remainingBalance;
        
        public PaymentResult(boolean success, String message, double remainingBalance) {
            this.success = success;
            this.message = message;
            this.remainingBalance = remainingBalance;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public double getRemainingBalance() {
            return remainingBalance;
        }
    }
}