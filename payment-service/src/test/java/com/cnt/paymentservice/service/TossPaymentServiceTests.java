package com.cnt.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.PaymentGateway;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.toss.TossConfirmReq;
import com.cnt.paymentservice.dto.toss.TossPaymentRes;
import com.cnt.paymentservice.infrastructure.TossClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TossPaymentServiceTests {

    @Mock
    TossClient tossClient;
    @Mock CouponService couponService;
    @Mock PaymentService paymentService;

    @InjectMocks
    TossPaymentService tossPaymentService;

    final Long memberId = 1L;

    @Test
    void confirm_success_without_coupon() {
        // given
        TossConfirmReq req = new TossConfirmReq(memberId, "payKey", "order-1", 10000, null);
        TossPaymentRes tossRes = new TossPaymentRes("payKey", "order-1", 10000, "DONE");
        PaymentRes expected = new PaymentRes(1L, 10000, 0, 10000);

        given(tossClient.confirm(req)).willReturn(tossRes);
        given(paymentService.process(
            PaymentGateway.TOSS,
            "payKey",
            "order-1",
            memberId,
            10000,
            0,
            null
        ))
            .willReturn(expected);

        // when
        PaymentRes res = tossPaymentService.confirm(req);

        // then
        assertThat(res).isEqualTo(expected);
        then(couponService).should(never()).findIfPresent(anyString(), anyLong());
        then(tossClient).should().confirm(req);
    }

    @Test
    void confirm_success_with_coupon() {
        // given
        TossConfirmReq req = new TossConfirmReq(memberId, "payKey", "order-2", 10000, "SAVE20");
        TossPaymentRes tossRes = new TossPaymentRes("payKey", "order-2", 8000, "DONE");
        Coupon coupon = mock(Coupon.class);
        PaymentRes expected = new PaymentRes(2L, 10000, 2000, 8000);

        given(coupon.calcDiscount(10000)).willReturn(2000);
        given(tossClient.confirm(req)).willReturn(tossRes);
        given(couponService.findIfPresent("SAVE20", memberId)).willReturn(coupon);
        given(paymentService.process(
            PaymentGateway.TOSS,
            "payKey",
            "order-2",
            memberId,
            10000,
            2000,
            coupon
        ))
            .willReturn(expected);

        // when
        PaymentRes res = tossPaymentService.confirm(req);

        // then
        assertThat(res).isEqualTo(expected);
        then(coupon).should().calcDiscount(10000);
    }
}
