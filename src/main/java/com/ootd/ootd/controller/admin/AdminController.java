package com.ootd.ootd.controller.admin;

import com.ootd.ootd.model.dto.coupon.DeleteCouponDTO;
import com.ootd.ootd.model.dto.coupon.InsertCouponDTO;
import com.ootd.ootd.model.dto.coupon.UpdateCouponDTO;
import com.ootd.ootd.model.dto.product.AdminProductDTO;

import com.ootd.ootd.service.coupon.CouponService;
import com.ootd.ootd.service.product.ProductService;
import com.ootd.ootd.service.promotion.ProductPromotionService;
import com.ootd.ootd.utils.service.GoogleCloudStorageService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    private final CouponService couponService;

    public AdminController(CouponService couponService) {
        this.couponService = couponService;
    }

    
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

