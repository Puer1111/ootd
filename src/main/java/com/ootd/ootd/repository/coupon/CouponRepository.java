package com.ootd.ootd.repository.coupon;

import com.ootd.ootd.model.entity.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}