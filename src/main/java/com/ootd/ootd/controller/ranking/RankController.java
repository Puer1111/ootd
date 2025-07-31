package com.ootd.ootd.controller.ranking;

import com.ootd.ootd.service.ranking.RankingService;
import com.ootd.ootd.service.ranking.impl.RankingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class RankController {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private RankingServiceImpl rankingServiceImpl;

    @GetMapping("/ranking")
    public String rankingPage() {
        return "view/ranking/ranking";
    }

    @GetMapping("/api/ranking/products")
    @ResponseBody
    public ResponseEntity<?> getProductRanking(@RequestParam(defaultValue = "reviews") String sortBy) {
        try {
            Map<String, Object> result;

            result = rankingServiceImpl.getProductRankingBySortType(sortBy);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/api/ranking/products/category")
    @ResponseBody
    public ResponseEntity<?> getProductRankingByCategory(
            @RequestParam(required = false) String mainCategory,
            @RequestParam(required = false) String subCategory,
            @RequestParam(defaultValue = "reviews") String sortBy) {
        try {
            Map<String, Object> result;

            result = rankingServiceImpl.getProductRankingByCategoryAndSortType(mainCategory, subCategory, sortBy);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "카테고리별 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/api/ranking/products/recommended")
    @ResponseBody
    public ResponseEntity<?> getRecommendedProductRanking() {
        try {
            Map<String, Object> result = rankingService.getRecommendedProductRanking();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "추천 상품 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/api/ranking/products/sale")
    @ResponseBody
    public ResponseEntity<?> getSaleProductRanking() {
        try {
            Map<String, Object> result = rankingService.getSaleProductRanking();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "세일 상품 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/api/ranking/categories/main")
    @ResponseBody
    public ResponseEntity<?> getMainCategories() {
        try {
            Map<String, Object> result = rankingService.getMainCategories();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "메인 카테고리 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/api/ranking/categories/sub")
    @ResponseBody
    public ResponseEntity<?> getSubCategories(@RequestParam String mainCategory) {
        try {
            Map<String, Object> result = rankingService.getSubCategories(mainCategory);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "하위 카테고리 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

}