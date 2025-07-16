package com.ootd.ootd.repository.coupon;

import com.ootd.ootd.model.entity.coupon.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
