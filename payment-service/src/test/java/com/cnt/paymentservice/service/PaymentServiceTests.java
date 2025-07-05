package com.cnt.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.Member;
import com.cnt.paymentservice.domain.Payment;
import com.cnt.paymentservice.domain.PaymentGateway;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.infrastructure.repository.MemberRepository;
import com.cnt.paymentservice.infrastructure.repository.PaymentRepository;
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

    @InjectMocks
    PaymentService paymentService;

    final Member member = new Member("Alice");

    @Test
    void process_success_with_coupon() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(paymentRepository.existsByPaymentKey("pgKey")).willReturn(false);
        given(paymentRepository.save(any(Payment.class)))
            .willAnswer(inv -> inv.getArgument(0));

        Coupon coupon = mock(Coupon.class);

        // when
        PaymentRes res = paymentService.process(
            PaymentGateway.TOSS,
            "pgKey", "order-1",
            1L,
            10_000,
            2_000,
            coupon);

        // then
        assertThat(res.paidAmount()).isEqualTo(8_000);
        assertThat(res.discountAmount()).isEqualTo(2_000);
        assertThat(member.getPoint()).isEqualTo(10_000);
        then(coupon).should().markUsed();
    }

    @Test
    void should_throw_when_member_not_found() {
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.process(
            PaymentGateway.TOSS, "pg", "order", 99L, 1_000, 0, null))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void should_throw_on_duplicate_payment_key() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(paymentRepository.existsByPaymentKey("dupKey")).willReturn(true);

        assertThatThrownBy(() -> paymentService.process(
            PaymentGateway.TOSS, "dupKey", "order", 1L, 1_000, 0, null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("이미 처리");
    }
}