package com.cnt.paymentservice.dto.coupon;

import com.cnt.paymentservice.domain.CouponType;
import java.time.LocalDateTime;

public record CouponIssueReq(
    String code,
    CouponType type,
    int discountValue,
    int maxDiscountAmount,
    LocalDateTime expiresAt
) {

}
