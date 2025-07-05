package com.cnt.paymentservice.service;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.Member;
import com.cnt.paymentservice.dto.coupon.CouponIssueReq;
import com.cnt.paymentservice.dto.coupon.CouponRes;
import com.cnt.paymentservice.infrastructure.repository.CouponRepository;
import com.cnt.paymentservice.infrastructure.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;

    public Coupon findIfPresent(String couponCode, Long memberId) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }

        Coupon coupon = couponRepository.findByCodeAndMemberId(couponCode, memberId)
            .orElseThrow(() -> new EntityNotFoundException("해당 쿠폰이 존재하지 않습니다."));

        validateCoupon(coupon);

        return coupon;
    }

    private void validateCoupon(Coupon coupon) {
        if (coupon.isUsed()) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("쿠폰이 만료되었습니다.");
        }
    }

    public CouponRes issueCouponToMember(CouponIssueReq req, Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));

        couponRepository.findByCodeAndMember(req.code(), member)
            .ifPresent(c -> {
                throw new IllegalStateException("이미 발급된 쿠폰입니다.");
            });

        Coupon coupon = new Coupon(
            req.code(),
            req.type(),
            req.discountValue(),
            req.maxDiscountAmount(),
            req.expiresAt(),
            false,
            member
        );

        return CouponRes.from(couponRepository.save(coupon));
    }

    public List<CouponRes> getCouponsForMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        return couponRepository.findAllByMember(member).stream().map(CouponRes::from).toList();
    }
}
