package com.cnt.paymentservice.dto.toss;

public record TossConfirmReq(
    Long   memberId,
    String paymentKey,
    String orderId,
    int    chargeAmount,
    String couponCode
) {}
