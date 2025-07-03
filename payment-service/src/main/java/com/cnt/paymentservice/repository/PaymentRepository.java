package com.cnt.paymentservice.repository;

import com.cnt.paymentservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByPaymentKey(String paymentKey);
}
