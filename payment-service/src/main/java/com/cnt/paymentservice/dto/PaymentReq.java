package com.cnt.paymentservice.dto;

public record PaymentReq(
    Long memberId,
    String paymentKey,
    String orderId,
    int amount
) {

}
