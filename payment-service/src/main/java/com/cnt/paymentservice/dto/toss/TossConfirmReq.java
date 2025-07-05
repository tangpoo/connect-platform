package com.cnt.paymentservice.dto.toss;

public record TossConfirmReq(
    String paymentKey,
    String orderId,
    int amount
) {

}
