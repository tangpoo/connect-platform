package com.cnt.paymentservice.controller;


import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.kakao.KakaoApproveReq;
import com.cnt.paymentservice.dto.kakao.KakaoApproveRes;
import com.cnt.paymentservice.service.KakaoPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/kakao")
@Tag(name = "KakaoPay API")
public class KakaoPaymentController {

    private final KakaoPaymentService kakaoPayService;

    @PostMapping("/approve")
    @Operation(summary = "결제 승인")
    public ResponseEntity<PaymentRes> approve(@RequestBody KakaoApproveReq req) {
        return ResponseEntity.ok(kakaoPayService.approve(req));
    }
}
