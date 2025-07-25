package com.ootd.ootd.controller.admin;

import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.service.promotion.ProductPromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/promotion")
public class AdminPromotionController {

    @Autowired
    private ProductPromotionService promotionService;

    
    @GetMapping("")
    public String adminPromotionPage() {
        return "view/admin/promotion/adminPromotion";
    }

    /**
     
     */
    @PostMapping("/recommend/{productNo}")
    @ResponseBody
    public ResponseEntity<?> setRecommended(@PathVariable Long productNo,
                                            @RequestBody Map<String, Object> request) {
        try {
            Boolean isRecommended = (Boolean) request.get("isRecommended");
            Integer priority = request.get("priority") != null ?
                    Integer.parseInt(request.get("priority").toString()) : 0;

            ProductPromotionDTO result = promotionService.setRecommended(productNo, isRecommended, priority);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", isRecommended ? "추천 상품으로 설정되었습니다." : "추천 상품에서 해제되었습니다.");
            response.put("promotion", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "추천 설정 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    
    @PostMapping("/sale/{productNo}")
    @ResponseBody
    public ResponseEntity<?> setSale(@PathVariable Long productNo,
                                     @RequestBody Map<String, Object> request) {
        try {
            Boolean isSale = (Boolean) request.get("isSale");
            Integer salePercentage = request.get("salePercentage") != null ?
                    Integer.parseInt(request.get("salePercentage").toString()) : null;
            Integer originalPrice = request.get("originalPrice") != null ?
                    Integer.parseInt(request.get("originalPrice").toString()) : null;

            ProductPromotionDTO result = promotionService.setSale(productNo, isSale, salePercentage, originalPrice);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", isSale ? "세일 상품으로 설정되었습니다." : "세일에서 해제되었습니다.");
            response.put("promotion", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "세일 설정 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    
    @GetMapping("/{productNo}")
    @ResponseBody
    public ResponseEntity<?> getPromotion(@PathVariable Long productNo) {
        try {
            ProductPromotionDTO promotion = promotionService.getPromotionByProductNo(productNo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("promotion", promotion);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "프로모션 정보 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    
    @GetMapping("/recommended")
    @ResponseBody
    public ResponseEntity<?> getRecommendedProducts() {
        try {
            List<ProductPromotionDTO> promotions = promotionService.getRecommendedProducts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("promotions", promotions);
            response.put("totalCount", promotions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "추천 상품 목록 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    
    @GetMapping("/sale")
    @ResponseBody
    public ResponseEntity<?> getSaleProducts() {
        try {
            List<ProductPromotionDTO> promotions = promotionService.getSaleProducts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("promotions", promotions);
            response.put("totalCount", promotions.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "세일 상품 목록 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    
    @DeleteMapping("/{productNo}")
    @ResponseBody
    public ResponseEntity<?> deletePromotion(@PathVariable Long productNo) {
        try {
            promotionService.deletePromotion(productNo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로모션 정보가 삭제되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "프로모션 삭제 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    
    @PostMapping("/cleanup-expired")
    @ResponseBody
    public ResponseEntity<?> cleanupExpiredSales() {
        try {
            promotionService.cleanupExpiredSales();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "만료된 세일이 정리되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "세일 정리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
}