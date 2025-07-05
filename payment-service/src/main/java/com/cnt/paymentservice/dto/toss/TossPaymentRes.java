package com.cnt.paymentservice.dto.toss;

public record TossPaymentRes(
    String paymentKey,
    String orderId,
    int totalAmount,
    String status
) {

}

