package com.cnt.paymentservice.service;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.Member;
import com.cnt.paymentservice.domain.Payment;
import com.cnt.paymentservice.domain.PaymentGateway;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.kakao.KakaoApproveReq;
import com.cnt.paymentservice.dto.kakao.KakaoApproveRes;
import com.cnt.paymentservice.infrastructure.repository.MemberRepository;
import com.cnt.paymentservice.infrastructure.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public PaymentRes process(
        PaymentGateway gateway,
        String pgKey,
        String orderId,
        Long memberId,
        int chargeAmount,
        int discountAmount,
        Coupon coupon) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));

        if (paymentRepository.existsByPaymentKey(pgKey)) {
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }

        int paidAmount = chargeAmount - discountAmount;
        Payment payment = new Payment(
            pgKey,
            orderId,
            chargeAmount,
            paidAmount,
            discountAmount,
            member,
            coupon,
            gateway
        );
        paymentRepository.save(payment);

        member.chargePoint(chargeAmount);

        if (coupon != null) {
            coupon.markUsed();
        }

        return new PaymentRes(
            payment.getId(),
            member.getPoint(),
            discountAmount,
            paidAmount
        );
    }

    public void validateAmountMatches(int totalAmount, int discount, int resultAmount) {
        int expectedPaidAmount = totalAmount - discount;
        if (resultAmount != expectedPaidAmount) {
            throw new IllegalStateException("결제 금액 불일치");
        }
    }
}
