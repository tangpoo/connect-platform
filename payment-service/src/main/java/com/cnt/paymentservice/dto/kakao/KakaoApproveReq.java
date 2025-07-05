package com.cnt.paymentservice.dto.kakao;

public record KakaoApproveReq(
    Long   memberId,
    String cid,
    String tid,
    String partnerOrderId,
    String partnerUserId,
    String pgToken,
    Integer totalAmount,
    String couponCode
) {}
