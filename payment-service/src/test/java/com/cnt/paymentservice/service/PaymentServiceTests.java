package com.cnt.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.Member;
import com.cnt.paymentservice.domain.Payment;
import com.cnt.paymentservice.dto.PaymentReq;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.TossPaymentRes;
import com.cnt.paymentservice.repository.MemberRepository;
import com.cnt.paymentservice.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock MemberRepository   memberRepository;
    @Mock PaymentRepository  paymentRepository;
    @Mock TossClientService  tossClientService;
    @Mock CouponService      couponService;

    @InjectMocks
    PaymentService paymentService;

    final Long   memberId = 1L;
    final Member member   = new Member("Alice");

    @Test
    void confirm_payment_success_without_coupon() {
        // given
        PaymentReq req = new PaymentReq(memberId, "payKey", "order-1", 10_000, null);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-1", 10_000, "DONE"));
        given(paymentRepository.existsByPaymentKey("payKey")).willReturn(false);
        given(paymentRepository.save(any(Payment.class)))
            .willAnswer(inv -> inv.getArgument(0));   // 그대로 반환

        // when
        PaymentRes res = paymentService.confirmPayment(req);

        // then
        assertThat(res.currentPoint()).isEqualTo(10_000);   // 포인트 전액 적립
        assertThat(res.discountAmount()).isZero();
        assertThat(res.paidAmount()).isEqualTo(10_000);
        then(couponService).should(never()).validateForUse(anyString(), any());
    }

    @Test
    void confirm_payment_success_with_coupon() {
        // given
        PaymentReq req = new PaymentReq(memberId, "payKey", "order-2", 10_000, "SAVE20");

        Coupon coupon = Mockito.mock(Coupon.class);
        given(coupon.calcDiscount(10_000)).willReturn(2_000);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(couponService.validateForUse("SAVE20", member)).willReturn(coupon);

        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-2", 8_000, "DONE"));

        given(paymentRepository.existsByPaymentKey("payKey")).willReturn(false);
        given(paymentRepository.save(any(Payment.class)))
            .willAnswer(inv -> inv.getArgument(0));

        // when
        PaymentRes res = paymentService.confirmPayment(req);

        // then
        assertThat(res.currentPoint()).isEqualTo(10_000);  // 할인 전 금액 전액 적립
        assertThat(res.discountAmount()).isEqualTo(2_000);
        assertThat(res.paidAmount()).isEqualTo(8_000);

        then(couponService).should().validateForUse("SAVE20", member);
        then(coupon).should().calcDiscount(10_000);
    }

    @Test
    void confirm_payment_should_throw_when_member_not_found() {
        PaymentReq req = new PaymentReq(memberId, "payKey", "order-1", 10_000, null);
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("회원이 존재하지 않습니다");
    }

    @Test
    void confirm_payment_should_throw_when_toss_response_null() {
        PaymentReq req = new PaymentReq(memberId, "payKey", "order-1", 10_000, null);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any())).willReturn(null);

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("승인 실패");
    }

    @Test
    void confirm_payment_should_throw_when_status_not_done() {
        PaymentReq req = new PaymentReq(memberId, "payKey", "order-1", 10_000, null);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-1", 10_000, "CANCELED"));

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("승인 실패");
    }

    @Test
    void confirm_payment_should_throw_when_amount_mismatch() {
        PaymentReq req = new PaymentReq(memberId, "payKey", "order-1", 10_000, null);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-1", 5_000, "DONE")); // 5 000 ≠ 10 000

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("금액 불일치");
    }

    @Test
    void confirm_payment_should_throw_when_duplicate_payment_key() {
        PaymentReq req = new PaymentReq(memberId, "payKey", "order-1", 10_000, null);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-1", 10_000, "DONE"));
        given(paymentRepository.existsByPaymentKey("payKey")).willReturn(true);

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("이미 처리");
    }
}
