package com.ootd.ootd.controller.admin;

import com.ootd.ootd.model.dto.coupon.DeleteCouponDTO;
import com.ootd.ootd.model.dto.coupon.InsertCouponDTO;
import com.ootd.ootd.model.dto.coupon.UpdateCouponDTO;
import com.ootd.ootd.model.dto.product.AdminProductDTO;
import com.ootd.ootd.model.dto.product.ProductDTO;
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

    // 상품 등록
    @PostMapping("/insert/products")
    public ResponseEntity<?> insertProduct(@ModelAttribute ProductDTO dto,
                                           HttpServletRequest request) {
        ProductDTO productDTO;
        try {
            if (dto.getImages() != null && dto.getImages().length > 0) {
                dto.setImageUrls(googleCloudStorageService.uploadImages(dto.getImages()));
            }
            productDTO = productService.insertProduct(dto);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("product", productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 상품 수정
    // AdminController.java의 수정 메서드

    @PutMapping("/update/products/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                           @ModelAttribute ProductDTO dto) {
        try {
            // 이미지 처리
            if (dto.getImages() != null && dto.getImages().length > 0) {
                dto.setImageUrls(googleCloudStorageService.uploadImages(dto.getImages()));
            }

            // 상품 정보 업데이트
            ProductDTO updatedProduct = productService.updateProduct(productId, dto);

            // 세일 정보가 있으면 프로모션 서비스로 업데이트
            if (dto.getIsActiveSale() != null) {
                ProductPromotionDTO promotionDTO = new ProductPromotionDTO();
                promotionDTO.setProductNo(productId);
                promotionDTO.setIsSale(dto.getIsActiveSale());
                promotionDTO.setIsActiveSale(dto.getIsActiveSale());
                promotionDTO.setSalePercentage(dto.getSalePercentage());
                promotionDTO.setOriginalPrice(updatedProduct.getPrice());

                if (dto.getIsActiveSale() && dto.getSalePercentage() != null) {
                    int salePrice = updatedProduct.getPrice() -
                            (updatedProduct.getPrice() * dto.getSalePercentage() / 100);
                    promotionDTO.setSalePrice(salePrice);
                }

                // 프로모션 서비스 호출
                promotionService.setSale(productId,
                        dto.getIsActiveSale(),
                        dto.getSalePercentage(),
                        updatedProduct.getPrice());
            }

            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 상품 삭제
    @DeleteMapping("/delete/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

/// / 상품 수정
//@PutMapping("/update/products/{productId}")
//public ResponseEntity<?> updateProduct(@PathVariable Long productId, @ModelAttribute ProductDTO dto) {
//    try {
//        ProductDTO updatedProduct = productService.updateProduct(productId, dto);
//        return ResponseEntity.ok(updatedProduct);
//    } catch (Exception e) {
//        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}
//
/// / 상품 삭제
//@DeleteMapping("/delete/products/{productId}")
//public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
//    try {
//        productService.deleteProduct(productId);
//        return ResponseEntity.ok().build();
//    } catch (Exception e) {
//        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}

