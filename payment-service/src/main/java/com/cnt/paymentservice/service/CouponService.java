package com.cnt.paymentservice.service;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.Member;
import com.cnt.paymentservice.dto.coupon.CouponIssueReq;
import com.cnt.paymentservice.dto.coupon.CouponRes;
import com.cnt.paymentservice.repository.CouponRepository;
import com.cnt.paymentservice.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;

    public Coupon validateForUse(String code, Member member) {
        Coupon coupon = couponRepository.findByCode(code)
            .orElseThrow(() -> new EntityNotFoundException("쿠폰이 존재하지 않습니다."));
        if (coupon.isUsed()) {
            throw new IllegalStateException("이미 사용한 쿠폰입니다.");
        }
        if (coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("쿠폰이 만료되었습니다.");
        }

        if (coupon.getMember() != null && !coupon.getMember().equals(member)) {
            throw new IllegalStateException("회원이 소유한 쿠폰이 아닙니다.");
        }

        return coupon;

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

    public void markUsed(Coupon coupon) {
        coupon.markUsed();
    }
}
