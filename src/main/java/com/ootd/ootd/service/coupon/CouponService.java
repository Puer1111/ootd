package com.ootd.ootd.service.coupon;

import com.ootd.ootd.model.dto.coupon.CouponResponseDTO;
import com.ootd.ootd.model.dto.coupon.InsertCouponDTO;
import com.ootd.ootd.model.dto.coupon.UpdateCouponDTO;

import java.util.List;

public interface CouponService {
    void insertCoupon(InsertCouponDTO insertCouponDTO);

    void updateCoupon(UpdateCouponDTO updateCouponDTO);

    void deleteCoupon(Long couponId);

    List<CouponResponseDTO> getAllCoupons();
}
