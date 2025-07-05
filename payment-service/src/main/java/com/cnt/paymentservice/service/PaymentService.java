package com.cnt.paymentservice.service;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.Member;
import com.cnt.paymentservice.domain.Payment;
import com.cnt.paymentservice.dto.PaymentReq;
import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.toss.TossConfirmReq;
import com.cnt.paymentservice.dto.toss.TossPaymentRes;
import com.cnt.paymentservice.repository.MemberRepository;
import com.cnt.paymentservice.repository.PaymentRepository;
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
    private final TossClientService tossClientService;
    private final CouponService couponService;

    public PaymentRes confirmPayment(PaymentReq req) {
        Member member = getMember(req.memberId());
        Coupon coupon = getValidCoupon(req.couponCode(), member);
        int discountAmount = calculateDiscount(req.chargeAmount(), coupon);
        int paidAmount = req.chargeAmount() - discountAmount;

        TossPaymentRes res = confirmWithToss(req, paidAmount);
        validateNotDuplicated(res.paymentKey());

        Payment payment = savePayment(req.chargeAmount(), paidAmount, discountAmount, member,
            coupon, res);
        member.chargePoint(payment.getChargeAmount());

        return new PaymentRes(payment.getId(), member.getPoint(), discountAmount, paidAmount);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
    }

    private void validateTossResponse(TossPaymentRes res, int expectedAmount) {
        if (res == null || !"DONE".equalsIgnoreCase(res.status())) {
            throw new IllegalStateException("Toss 결제 승인 실패");
        }
        if (res.totalAmount() != expectedAmount) {
            throw new IllegalStateException("결제 금액 불일치");
        }
    }

    private Coupon getValidCoupon(String code, Member member) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return couponService.validateForUse(code, member);
    }

    private int calculateDiscount(int chargeAmount, Coupon coupon) {
        return (coupon != null) ? coupon.calcDiscount(chargeAmount) : 0;
    }

    private TossPaymentRes confirmWithToss(PaymentReq req, int expectedAmount) {
        TossConfirmReq confirmReq = new TossConfirmReq(req.paymentKey(), req.orderId(),
            expectedAmount);
        TossPaymentRes res = tossClientService.confirm(confirmReq);
        validateTossResponse(res, expectedAmount);
        return res;
    }

    private void validateNotDuplicated(String paymentKey) {
        if (paymentRepository.existsByPaymentKey(paymentKey)) {
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }
    }

    private Payment savePayment(int chargeAmount, int paidAmount, int discountAmount,
        Member member, Coupon coupon, TossPaymentRes res) {
        Payment pay = new Payment(
            res.paymentKey(), res.orderId(),
            chargeAmount, paidAmount, discountAmount,
            member, coupon);
        return paymentRepository.save(pay);
    }
}
