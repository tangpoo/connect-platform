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

    private int amount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Payment(final String paymentKey, final String orderId, final int amount,
        final Member member) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.member = member;
    }
}
