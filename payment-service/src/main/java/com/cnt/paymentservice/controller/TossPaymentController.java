package com.cnt.paymentservice.controller;

import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.toss.TossConfirmReq;
import com.cnt.paymentservice.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentRes> confirmPayment(@RequestBody TossConfirmReq req) {
        return ResponseEntity.ok(tossPaymentService.confirm(req));
    }
}
