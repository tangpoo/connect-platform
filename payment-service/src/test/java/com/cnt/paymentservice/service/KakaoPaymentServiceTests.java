package com.cnt.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.PaymentGateway;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.kakao.Amount;
import com.cnt.paymentservice.dto.kakao.KakaoApproveReq;
import com.cnt.paymentservice.dto.kakao.KakaoApproveRes;
import com.cnt.paymentservice.infrastructure.KakaoPayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoPaymentServiceTests {

    @Mock
    KakaoPayClient kakaoClient;
    @Mock CouponService  couponService;
    @Mock PaymentService paymentService;

    @InjectMocks
    KakaoPaymentService kakaoPaymentService;

    @Test
    void approve_success_without_coupon() {
        // given
        KakaoApproveReq req = new KakaoApproveReq(
            1L,
            "TC0ONETIME",
            "T123",
            "order-3",
            "user1",
            "pgToken",
            10000,
            null
        );

        Amount amt = new Amount(10000, 0, 0, 0, 0);
        KakaoApproveRes kakaoRes = new KakaoApproveRes(
            "aid",
            "T123",
            "TC0ONETIME",
            "CARD",
            amt,
            "item",
            "user1",
            "order-3",
            "2025-07-05T12:00:00"
        );

        PaymentRes expected = new PaymentRes(3L, 10000, 0, 10000);

        given(kakaoClient.approve(req)).willReturn(kakaoRes);
        given(paymentService.process(
            PaymentGateway.KAKAO,
            "T123",
            "order-3",
            1L,
            10000,
            0,
            null
        )).willReturn(expected);

        // when
        PaymentRes res = kakaoPaymentService.approve(req);

        // then
        assertThat(res).isEqualTo(expected);
        then(kakaoClient).should().approve(req);
    }

    @Test
    void approve_success_with_coupon() {
        // given
        KakaoApproveReq req = new KakaoApproveReq(
            1L,
            "TC0ONETIME",
            "T123",
            "order-3",
            "user1",
            "pgToken",
            8000,
            "WELCOME"
        );

        Amount amt = new Amount(8000, 0, 0, 0, 0);
        KakaoApproveRes kakaoRes = new KakaoApproveRes(
            "aid",
            "T123",
            "TC0ONETIME",
            "CARD",
            amt,
            "item",
            "user1",
            "order-3",
            "2025-07-05T12:00:00"
        );

        Coupon coupon = mock(Coupon.class);

        PaymentRes expected = new PaymentRes(3L, 10000, 2000, 8000);

        given(kakaoClient.approve(req)).willReturn(kakaoRes);
        given(coupon.calcDiscount(8000)).willReturn(2000);
        given(couponService.findIfPresent("WELCOME", 1L)).willReturn(coupon);
        given(paymentService.process(
            PaymentGateway.KAKAO,
            "T123",
            "order-3",
            1L,
            10000,
            2000,
            coupon
        )).willReturn(expected);

        // when
        PaymentRes res = kakaoPaymentService.approve(req);

        // then
        assertThat(res).isEqualTo(expected);
        then(kakaoClient).should().approve(req);
    }
}
