package com.cnt.paymentservice.service;

import com.cnt.paymentservice.domain.Member;
import com.cnt.paymentservice.domain.Payment;
import com.cnt.paymentservice.dto.PaymentReq;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.TossConfirmReq;
import com.cnt.paymentservice.dto.TossPaymentRes;
import com.cnt.paymentservice.repository.MemberRepository;
import com.cnt.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final TossClientService tossClientService;

    @Transactional
    public PaymentRes confirmPayment(PaymentReq req) {
        Member member = getMember(req.memberId());

        TossConfirmReq confirmReq = new TossConfirmReq(
            req.paymentKey(), req.orderId(), req.amount());
        TossPaymentRes res = tossClientService.tossConfirmPayment(confirmReq);

        validateTossResponse(res, req.amount());

        return saveAndCharge(member, res);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
    }

    private void validateTossResponse(TossPaymentRes res, int expectedAmount) {
        if (res == null || !"DONE".equalsIgnoreCase(res.status())) {
            throw new IllegalStateException("Toss 결제 승인 실패");
        }
        if (res.totalAmount() != expectedAmount) {
            throw new IllegalStateException("결제 금액 불일치");
        }
    }

    private PaymentRes saveAndCharge(Member member, TossPaymentRes res) {

        if (paymentRepository.existsByPaymentKey(res.paymentKey())) {
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }

        Payment pay = paymentRepository.save(
            new Payment(res.paymentKey(), res.orderId(), res.totalAmount(), member));

        member.chargePoint(res.totalAmount());

        return new PaymentRes(pay.getId(), member.getPoint());
    }
}
