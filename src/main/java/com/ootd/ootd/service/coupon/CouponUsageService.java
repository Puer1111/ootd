package com.ootd.ootd.service.coupon;

import com.ootd.ootd.model.dto.coupon.CouponDetailsDto;

import java.util.List;

public interface CouponUsageService {
    List<CouponDetailsDto> getAvailableCouponsForUser(Long userId);
}
