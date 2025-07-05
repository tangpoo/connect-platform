package com.cnt.paymentservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentKey;

    private String orderId;

    private int chargeAmount;

    private int paidAmount;

    private int discountAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    public Payment(
        String paymentKey,
        String orderId,
        int chargeAmount,
        int paidAmount,
        int discountAmount,
        Member member,
        Coupon coupon
    ) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.chargeAmount = chargeAmount;
        this.paidAmount = paidAmount;
        this.discountAmount = discountAmount;
        this.member = member;
        this.coupon = coupon;
    }
}
