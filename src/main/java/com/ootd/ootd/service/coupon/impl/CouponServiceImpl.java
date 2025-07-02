package com.ootd.ootd.service.coupon.impl;

import com.ootd.ootd.model.dto.coupon.InsertCouponDTO;
import com.ootd.ootd.model.entity.category.Category;
import com.ootd.ootd.model.entity.coupon.Coupon;
import com.ootd.ootd.repository.category.CategoryRepository;
import com.ootd.ootd.repository.coupon.CouponRepository;
import com.ootd.ootd.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void insertCoupon(InsertCouponDTO insertCouponDTO) {
        Category category = categoryRepository.findBySubCategory(insertCouponDTO.getSubCategory())
                .orElseThrow(() -> new IllegalArgumentException("Invalid subCategory name: " + insertCouponDTO.getSubCategory()));

        Coupon coupon = Coupon.builder()
                .couponName(insertCouponDTO.getCouponName())
                .categoryId(category.getCategoryNo())
                .discountRate(insertCouponDTO.getDiscountRate())
                .quantity(insertCouponDTO.getQuantity())
                .expirationDate(insertCouponDTO.getExpirationDate())
                .receiveLimit(insertCouponDTO.getReceiveLimit())
                .usageLimit(insertCouponDTO.getUsageLimit())
                .build();
        couponRepository.save(coupon);
    }
}
