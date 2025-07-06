package com.cnt.paymentservice.service;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.PaymentGateway;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.kakao.KakaoApproveReq;
import com.cnt.paymentservice.dto.kakao.KakaoApproveRes;
import com.cnt.paymentservice.infrastructure.KakaoPayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KakaoPaymentService {

    private final KakaoPayClient kakaoClient;
    private final PaymentService paymentService;
    private final CouponService couponService;

    public PaymentRes approve(KakaoApproveReq req) {
        KakaoApproveRes kakaoRes = kakaoClient.approve(req);

        Coupon coupon = couponService.findIfPresent(req.couponCode(), req.memberId());
        int discount = (coupon != null) ? coupon.calcDiscount(kakaoRes.amount().total()) : 0;

        paymentService.validateAmountMatches(req.totalAmount(), discount, kakaoRes.amount().total());

        return paymentService.process(
            PaymentGateway.KAKAO,
            kakaoRes.tid(),
            kakaoRes.partnerOrderId(),
            req.memberId(),
            kakaoRes.amount().total() + discount,
            discount,
            coupon);
    }
}