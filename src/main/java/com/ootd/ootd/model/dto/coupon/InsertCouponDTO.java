package com.ootd.ootd.model.dto.coupon;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InsertCouponDTO {
    private String couponName;
    private String subCategory;
    private Integer discountRate;
    private Integer quantity;
    private LocalDate expirationDate;
    private Integer receiveLimit;
    private Integer usageLimit;
}
