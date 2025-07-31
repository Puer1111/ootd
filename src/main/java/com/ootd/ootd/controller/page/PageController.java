package com.ootd.ootd.controller.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ootd.ootd.service.promotion.ProductPromotionService;
import com.ootd.ootd.service.product.ProductService;
import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.model.dto.product.ProductDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private ProductPromotionService promotionService;

    @Autowired
    private ProductService productService;

    @GetMapping("/recommended")
    public String recommendedPage() {
        return "view/promotion/recommended";
    }

    @GetMapping("/sale")
    public String salePage() {
        return "view/promotion/sale";
    }

    
    @GetMapping("/api/promotion/recommended")
    @ResponseBody
    public ResponseEntity<?> getRecommendedProducts() {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== 추천 상품 API 호출 ===");

            List<ProductPromotionDTO> promotions = promotionService.getRecommendedProducts();
            System.out.println("추천 프로모션 개수: " + promotions.size());

            List<ProductDTO> products = new ArrayList<>();

            for (ProductPromotionDTO promotion : promotions) {
                try {
                    System.out.println("상품 조회 중 - productNo: " + promotion.getProductNo());
                    ProductDTO product = productService.getProductById(promotion.getProductNo());
                    if (product != null) {
                        System.out.println("상품 조회 성공: " + product.getProductName());

                        product.setPromotionInfo(promotion);

                        if (product.getIsRecommended() == null) {
                            product.setIsRecommended(true);
                        }
                        if (product.getIsActiveSale() == null) {
                            product.setIsActiveSale(false);
                        }
                        if (product.getIsSale() == null) {
                            product.setIsSale(false);
                        }

                        products.add(product);
                    } else {
                        System.err.println("상품 정보가 null - productNo: " + promotion.getProductNo());
                    }
                } catch (Exception e) {
                    System.err.println("상품 정보 조회 실패 - productNo: " + promotion.getProductNo() + ", 에러: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("최종 추천 상품 개수: " + products.size());

            response.put("success", true);
            response.put("products", products);
            response.put("totalCount", products.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("추천 상품 API 실패: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "추천 상품을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            response.put("products", new ArrayList<>());
            response.put("totalCount", 0);

            return ResponseEntity.ok(response);
        }
    }
}