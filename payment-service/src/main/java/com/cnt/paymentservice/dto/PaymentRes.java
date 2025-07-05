package com.cnt.paymentservice.dto;

public record PaymentRes(
    Long paymentId,
    int currentPoint,
    int discountAmount,
    int paidAmount
) {

}
