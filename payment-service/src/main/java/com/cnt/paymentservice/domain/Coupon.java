package com.cnt.paymentservice.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private int discountValue;

    private Integer maxDiscountAmount;

    private LocalDateTime expiresAt;

    private boolean used;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Coupon(
        String code,
        CouponType type,
        int discountValue,
        Integer maxDiscountAmount,
        LocalDateTime expiresAt,
        boolean used, Member member
    ) {
        this.code = code;
        this.type = type;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.expiresAt = expiresAt;
        this.used = used;
        this.member = member;
    }

    public int calcDiscount(int chargeAmount) {
        int raw = (type == CouponType.PERCENT)
            ? chargeAmount * discountValue / 100
            : discountValue;
        if (maxDiscountAmount != null) {
            raw = Math.min(raw, maxDiscountAmount);
        }
        return raw;
    }

    public void markUsed() {
        if (used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("쿠폰이 만료되었습니다");
        }
        this.used = true;
    }
}
