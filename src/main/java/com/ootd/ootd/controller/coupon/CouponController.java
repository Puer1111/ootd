package com.ootd.ootd.controller.coupon;

import com.ootd.ootd.model.dto.coupon.CouponDetailsDto;
import com.ootd.ootd.model.dto.coupon.DeleteCouponDTO;
import com.ootd.ootd.model.dto.coupon.InsertCouponDTO;
import com.ootd.ootd.model.dto.coupon.UpdateCouponDTO;
import com.ootd.ootd.repository.coupon.CouponUsageRepository;
import com.ootd.ootd.service.auth.user.impl.UserDetailsImpl;
import com.ootd.ootd.service.coupon.CouponService;
import com.ootd.ootd.service.coupon.CouponUsageService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponUsageService couponUsageService;

    @PostMapping("/insert")
    public ResponseEntity<Void> insertCoupon(@RequestBody InsertCouponDTO insertCouponDTO) {
        couponService.insertCoupon(insertCouponDTO);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateCoupon(@RequestBody UpdateCouponDTO updateCouponDTO) {
        couponService.updateCoupon(updateCouponDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCoupon(@RequestBody DeleteCouponDTO deleteCouponDTO) {
        couponService.deleteCoupon(deleteCouponDTO.getCouponId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PostMapping("/{couponId}/issue")
    public ResponseEntity<Void> issueCoupon(@PathVariable Long couponId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        couponService.issueCoupon(couponId, userId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }
    @GetMapping("/lookup")
    public ResponseEntity<?> lookupCoupon(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        List<CouponDetailsDto> result = couponUsageService.getAvailableCouponsForUser(userId);
        return ResponseEntity.ok(result);
    }
}
