package com.common_wealth_builders.exception;

public class PaymentAlreadyVerifiedException extends RuntimeException {
    public PaymentAlreadyVerifiedException(String message) {
        super(message);
    }
}