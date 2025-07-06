package com.cnt.paymentservice.controller;

import com.cnt.paymentservice.dto.PaymentRes;
import com.cnt.paymentservice.dto.toss.TossConfirmReq;
import com.cnt.paymentservice.service.TossPaymentService;
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
@RequestMapping("/api/v1/payments/toss")
@Tag(name = "TossPay")
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;

    @PostMapping("/confirm")
    @Operation(summary = "결제 승인")
    public ResponseEntity<PaymentRes> confirmPayment(@RequestBody TossConfirmReq req) {
        return ResponseEntity.ok(tossPaymentService.confirm(req));
    }
}
