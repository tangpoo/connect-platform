package com.cnt.paymentservice.infrastructure.repository;

import com.cnt.paymentservice.domain.Coupon;
import com.cnt.paymentservice.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findByCodeAndMember(String code, Member member);

    List<Coupon> findAllByMember(Member member);

    Optional<Coupon> findByCodeAndMemberId(String code, Long memberId);
}
