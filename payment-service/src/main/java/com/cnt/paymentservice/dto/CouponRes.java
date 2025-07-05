package com.cnt.paymentservice.dto;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.CouponType;
import java.time.LocalDateTime;

public record CouponRes(
    String code,
    CouponType type,
    int discountValue,
    Integer maxDiscountAmount,
    boolean used,
    LocalDateTime expiresAt
) {

    public static CouponRes from(Coupon c) {
        return new CouponRes(
            c.getCode(), c.getType(), c.getDiscountValue(),
            c.getMaxDiscountAmount(), c.isUsed(), c.getExpiresAt()
        );
    }
}
