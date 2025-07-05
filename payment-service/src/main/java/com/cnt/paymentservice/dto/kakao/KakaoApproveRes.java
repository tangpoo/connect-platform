package com.cnt.paymentservice.dto.kakao;

public record KakaoApproveRes(
    String aid,
    String tid,
    String cid,
    String paymentMethodType,
    Amount amount,
    String itemName,
    String partnerUserId,
    String partnerOrderId,
    String createdAt
) {

}
