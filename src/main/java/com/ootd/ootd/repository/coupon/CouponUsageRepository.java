package com.ootd.ootd.repository.coupon;

import com.ootd.ootd.model.entity.coupon.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);


    @Query("SELECT c.couponName , c.discountRate ,cp.receivedAt , c.expirationDate from CouponUsage cp join Coupon c on cp.couponId = c.couponId where cp.userId=:userId")
    List<Object[]> findCouponDetailsByUserId(Long userId);
}
