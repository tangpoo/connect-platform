package com.cnt.paymentservice.dto;

public record TossConfirmReq(
    String paymentKey,
    String orderId,
    int amount
) {

}
