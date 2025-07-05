package com.cnt.paymentservice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CouponTests {

    private Coupon newCoupon(
        CouponType type,
        int discountValue,
        Integer maxDiscount,
        LocalDateTime expiresAt,
        boolean used) {

        return new Coupon(
            "code",
            type,
            discountValue,
            maxDiscount,
            expiresAt,
            used,
            new Member()
        );
    }

    @Nested
    class CalcDiscount {

        @Test
        void percent_without_cap() {
            Coupon c = newCoupon(CouponType.PERCENT, 20, null,
                LocalDateTime.now().plusDays(1), false);

            int discount = c.calcDiscount(10_000);

            assertThat(discount).isEqualTo(2_000);
        }

        @Test
        void percent_with_cap() {
            Coupon c = newCoupon(CouponType.PERCENT, 30, 5_000,
                LocalDateTime.now().plusDays(1), false);

            int discount = c.calcDiscount(30_000);

            assertThat(discount).isEqualTo(5_000);
        }

        @Test
        void fixed_discount() {
            Coupon c = newCoupon(CouponType.FIXED, 3_000, null,
                LocalDateTime.now().plusDays(1), false);

            int discount = c.calcDiscount(10_000);

            assertThat(discount).isEqualTo(3_000);
        }
    }

    @Nested
    class MarkUsed {

        @Test
        void mark_used_success() {
            Coupon c = newCoupon(CouponType.FIXED, 1_000, null,
                LocalDateTime.now().plusDays(1), false);

            c.markUsed();

            assertThat(ReflectionTestUtils.getField(c, "used")).isEqualTo(true);
        }

        @Test
        void mark_used_should_throw_if_already_used() {
            Coupon c = newCoupon(CouponType.FIXED, 1_000, null,
                LocalDateTime.now().plusDays(1), true);

            assertThatThrownBy(c::markUsed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 사용된 쿠폰");
        }

        @Test
        void mark_used_should_throw_if_expired() {
            Coupon c = newCoupon(CouponType.FIXED, 1_000, null,
                LocalDateTime.now().minusDays(1), false);

            assertThatThrownBy(c::markUsed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("쿠폰이 만료");
        }
    }
}
