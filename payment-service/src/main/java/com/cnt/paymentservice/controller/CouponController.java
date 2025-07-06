package com.cnt.paymentservice.controller;

import com.cnt.paymentservice.dto.coupon.CouponIssueReq;
import com.cnt.paymentservice.dto.coupon.CouponRes;
import com.cnt.paymentservice.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupon API")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    @Operation(summary = "쿠폰 발급")
    public ResponseEntity<CouponRes> issue(
        @RequestParam Long memberId,
        @RequestBody CouponIssueReq req
    ) {
        return ResponseEntity.ok(couponService.issueCouponToMember(req, memberId));
    }

    @GetMapping("/my")
    @Operation(summary = "보유 쿠폰 조회")
    public ResponseEntity<List<CouponRes>> getMyCoupons(@RequestParam Long memberId) {
        return ResponseEntity.ok(couponService.getCouponsForMember(memberId));
    }
}
