package com.ootd.ootd.controller.admin;

import com.ootd.ootd.model.dto.coupon.DeleteCouponDTO;
import com.ootd.ootd.model.dto.coupon.InsertCouponDTO;
import com.ootd.ootd.model.dto.coupon.UpdateCouponDTO;
import com.ootd.ootd.model.dto.product.AdminProductDTO;
import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.dto.product.ProductResponseInfoDTO;
import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.service.coupon.CouponService;
import com.ootd.ootd.service.product.ProductService;
import com.ootd.ootd.service.promotion.ProductPromotionService;
import com.ootd.ootd.utils.service.GoogleCloudStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private GoogleCloudStorageService googleCloudStorageService;

    @Autowired
    private ProductPromotionService promotionService;

    private final CouponService couponService;

    public AdminController(CouponService couponService) {
        this.couponService = couponService;
    }

    // 쿠폰 관리자 페이지
    @GetMapping("/coupon")
    public String adminCouponPage() {
        return "view/admin/coupon/adminCoupon";
    }


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

    @GetMapping("/product")
    public String adminProductPage() {
        return "view/admin/product/adminProduct";
    }

    @GetMapping("/select/product")
    @ResponseBody
    public List<AdminProductDTO> getAdminProducts() {
        return productService.getAdminProducts();
    }

}

