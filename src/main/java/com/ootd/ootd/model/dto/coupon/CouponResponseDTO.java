package com.ootd.ootd.model.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponseDTO {
    private Long id;
    private String couponName;
    private Integer discountRate;
    private Integer quantity;
    private LocalDate expirationDate;
}
