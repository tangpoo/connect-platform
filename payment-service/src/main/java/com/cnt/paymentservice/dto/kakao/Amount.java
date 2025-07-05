package com.cnt.paymentservice.dto.kakao;

public record Amount(
    int total,
    int taxFree,
    int vat,
    int point,
    int discount
) {

}
