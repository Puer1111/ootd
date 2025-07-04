package com.ootd.ootd.controller.coupon;

import com.ootd.ootd.model.dto.coupon.DeleteCouponDTO;
import com.ootd.ootd.model.dto.coupon.InsertCouponDTO;
import com.ootd.ootd.model.dto.coupon.UpdateCouponDTO;
import com.ootd.ootd.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/modify")
    public ResponseEntity<Void> modifyCoupon(@RequestBody UpdateCouponDTO updateCouponDTO) {
        couponService.updateCoupon(updateCouponDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCoupon(@RequestBody DeleteCouponDTO deleteCouponDTO) {
        couponService.deleteCoupon(deleteCouponDTO.getCouponId());
        return ResponseEntity.ok().build();
    }
}
