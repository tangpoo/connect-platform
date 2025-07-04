package com.cnt.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock
    MemberRepository memberRepository;
    @Mock
    PaymentRepository paymentRepository;
    @Mock
    TossClientService tossClientService;
    @InjectMocks
    PaymentService paymentService;

    Long memberId = 1L;
    Member member = new Member("Alice");
    PaymentReq req = new PaymentReq(memberId, "payKey", "order-1", 10_000);

    @Test
    void confirm_payment_success() {
        // given
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-1", 10_000, "DONE"));
        given(paymentRepository.existsByPaymentKey("payKey")).willReturn(false);
        given(paymentRepository.save(any()))
            .willAnswer(inv -> {
                Payment p = inv.getArgument(0);
                return new Payment(p.getPaymentKey(), p.getOrderId(), p.getAmount(), member);
            });

        // when
        PaymentRes res = paymentService.confirmPayment(req);

        // then
        assertThat(res.currentPoint()).isEqualTo(10_000);
        then(paymentRepository).should().save(any(Payment.class));
    }

    @Test
    void confirm_payment_should_throw_when_member_not_found() {
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("회원이 존재하지 않습니다.");
    }

    @Test
    void confirm_payment_should_throw_when_toss_response_null() {
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any())).willReturn(null);

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("승인 실패");
    }

    @Test
    void confirm_payment_should_throw_when_status_not_done() {
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-1", 10_000, "CANCELED"));

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("승인 실패");
    }

    @Test
    void confirm_payment_should_throw_when_amount_mismatch() {
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-1", 5_000, "DONE"));

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("금액 불일치");
    }

    @Test
    void confirm_payment_should_throw_when_duplicate_payment_key() {
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(tossClientService.tossConfirmPayment(any()))
            .willReturn(new TossPaymentRes("payKey", "order-1", 10_000, "DONE"));
        given(paymentRepository.existsByPaymentKey("payKey")).willReturn(true);

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("이미 처리");
    }
}
