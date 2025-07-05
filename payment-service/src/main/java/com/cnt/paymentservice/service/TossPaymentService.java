package com.cnt.paymentservice.service;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.PaymentGateway;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.toss.TossConfirmReq;
import com.cnt.paymentservice.dto.toss.TossPaymentRes;
import com.cnt.paymentservice.infrastructure.TossClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TossPaymentService {

    private final TossClient tossClient;
    private final PaymentService paymentService;
    private final CouponService couponService;

    public PaymentRes confirm(TossConfirmReq req) {
        TossPaymentRes tossRes = tossClient.confirm(req);

        Coupon coupon = couponService.findIfPresent(req.couponCode(), req.memberId());
        int discount = (coupon != null) ? coupon.calcDiscount(req.chargeAmount()) : 0;

        return paymentService.process(
            PaymentGateway.TOSS,
            tossRes.paymentKey(),
            tossRes.orderId(),
            req.memberId(),
            req.chargeAmount(),
            discount,
            coupon);
    }
}
