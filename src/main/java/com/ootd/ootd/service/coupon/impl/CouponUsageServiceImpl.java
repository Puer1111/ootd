package com.ootd.ootd.service.coupon.impl;

import com.ootd.ootd.model.dto.coupon.CouponDetailsDto;
import com.ootd.ootd.repository.coupon.CouponUsageRepository;
import com.ootd.ootd.service.coupon.CouponUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponUsageServiceImpl implements CouponUsageService {

    private final CouponUsageRepository couponUsageRepository;

    @Override
    public List<CouponDetailsDto> getAvailableCouponsForUser(Long userId) {
        List<Object[]> results = couponUsageRepository.findCouponDetailsByUserId(userId);
        List<CouponDetailsDto> couponDetailsList = new ArrayList<>();

        for (Object[] row : results) {
            String couponName = (String) row[0];
            Integer discountRate = (Integer) row[1];
            LocalDateTime receivedAt = (LocalDateTime) row[2];
            LocalDate expiredAt = (LocalDate) row[3];

            Long remainingDays = null;

            if (receivedAt != null && expiredAt != null) {
                remainingDays = java.time.temporal.ChronoUnit.DAYS.between(receivedAt.toLocalDate(), expiredAt);
            }

            couponDetailsList.add(new CouponDetailsDto(couponName, discountRate, receivedAt, expiredAt, remainingDays));
        }
        return couponDetailsList;
    }
}
