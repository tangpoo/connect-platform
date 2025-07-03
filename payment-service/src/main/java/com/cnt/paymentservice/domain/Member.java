package com.cnt.paymentservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private int viewCount;

    private int point;

    public Member(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.viewCount = 0;
        this.point = 0;
    }

    public void increaseViewCount(Long count) {
        this.viewCount += count;
    }

    public void chargePoint(int amount) {
        this.point += amount;
    }
}
