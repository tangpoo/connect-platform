package com.cnt.paymentservice.controller;


import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.kakao.KakaoApproveReq;
import com.cnt.paymentservice.dto.kakao.KakaoApproveRes;
import com.cnt.paymentservice.service.KakaoPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/kakao")
public class KakaoPaymentController {

    private final KakaoPaymentService kakaoPayService;

    @PostMapping("/apporve")
    public ResponseEntity<PaymentRes> approve(@RequestBody KakaoApproveReq req) {
        return ResponseEntity.ok(kakaoPayService.approve(req));
    }
}
