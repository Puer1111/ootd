package com.ootd.ootd.repository.payment;

import com.ootd.ootd.model.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p.impUid FROM Payment p WHERE p.orderId = :orderId")
    String findByOrderId(Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.impUid = :impUid")
    Payment findByImpUid(@Param("impUid") String impUid);

    @Modifying
    @Transactional
    @Query("UPDATE Payment p SET p.paymentStatus = 'cancel' WHERE p.impUid = :impUid")
    int changeStatus(@Param("impUid") String impUid);
}