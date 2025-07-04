package com.ootd.ootd.model.dto.coupon;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCouponDTO {
    private Long couponId;
    private String couponName;
    private String subCategory;
    private Integer discountRate;
    private Integer quantity;
    private Integer receiveLimit;
    private Integer usageLimit;
    private LocalDate expirationDate;
}