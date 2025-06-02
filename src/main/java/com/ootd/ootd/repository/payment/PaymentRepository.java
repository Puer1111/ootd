package com.ootd.ootd.repository.payment;

import com.ootd.ootd.model.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
