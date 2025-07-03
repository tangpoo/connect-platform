package com.cnt.paymentservice.dto;

public record TossPaymentRes(
    String paymentKey,
    String orderId,
    int totalAmount,
    String status
) {

}

