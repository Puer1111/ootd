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
    private RankingServiceImpl rankingServiceImpl; // 🆕 추가 메서드 사용을 위해

    /**
     * 랭킹 페이지 이동
     */
    @GetMapping("/ranking")
    public String rankingPage() {
        return "view/ranking/ranking";
    }

    /**
     * 상품 랭킹 조회 (정렬 기준별)
     * @param sortBy reviews(리뷰수), likes(좋아요수), rating(평점) - 기본값: reviews
     */
    @GetMapping("/api/ranking/products")
    @ResponseBody
    public ResponseEntity<?> getProductRanking(@RequestParam(defaultValue = "reviews") String sortBy) {
        try {
            Map<String, Object> result;

            // 🆕 통합 메서드 사용
            result = rankingServiceImpl.getProductRankingBySortType(sortBy);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 카테고리별 상품 랭킹 조회
     * @param mainCategory 메인 카테고리
     * @param subCategory 하위 카테고리
     * @param sortBy 정렬 기준 (reviews, likes, rating)
     */
    @GetMapping("/api/ranking/products/category")
    @ResponseBody
    public ResponseEntity<?> getProductRankingByCategory(
            @RequestParam(required = false) String mainCategory,
            @RequestParam(required = false) String subCategory,
            @RequestParam(defaultValue = "reviews") String sortBy) {
        try {
            Map<String, Object> result;

            // 🆕 통합 메서드 사용
            result = rankingServiceImpl.getProductRankingByCategoryAndSortType(mainCategory, subCategory, sortBy);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "카테고리별 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 추천 상품 랭킹 조회
     */
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

    /**
     * 세일 상품 랭킹 조회
     */
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

    /**
     * 메인 카테고리 목록 조회
     */
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

    /**
     * 하위 카테고리 목록 조회
     */
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