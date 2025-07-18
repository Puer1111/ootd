package com.ootd.ootd.model.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponDetailsDto {
    private String couponName;
    private Integer discountRate;
    private LocalDateTime receivedAt;
    private LocalDate expiredAt;
    
    private Long remainingDays; // expiredAt - receivedAt (일 단위)
}
