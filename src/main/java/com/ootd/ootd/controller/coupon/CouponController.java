package com.ootd.ootd.controller.coupon;

import com.ootd.ootd.model.dto.coupon.InsertCouponDTO;
import com.ootd.ootd.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupon")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/insert")
    public ResponseEntity<Void> insertCoupon(@RequestBody InsertCouponDTO insertCouponDTO) {
        couponService.insertCoupon(insertCouponDTO);
        return ResponseEntity.ok().build();
    }
}
