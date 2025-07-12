//package com.ootd.ootd.controller.ranking;
//
//import com.ootd.ootd.service.ranking.RankingService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/ranking")
//public class RankingController {
//
//    @Autowired
//    private RankingService rankingService;
//
//    /**
//     * 전체 상품 랭킹 조회 (리뷰 기준)
//     */
//    @GetMapping("/products")
//    public ResponseEntity<?> getProductRanking() {
//        try {
//            Map<String, Object> result = rankingService.getProductRanking();
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * 카테고리별 상품 랭킹 조회
//     */
//    @GetMapping("/products/category")
//    public ResponseEntity<?> getProductRankingByCategory(
//            @RequestParam(required = false) String mainCategory,
//            @RequestParam(required = false) String subCategory) {
//        try {
//            Map<String, Object> result = rankingService.getProductRankingByCategory(mainCategory, subCategory);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "카테고리별 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * 추천 상품 랭킹 조회
//     */
//    @GetMapping("/products/recommended")
//    public ResponseEntity<?> getRecommendedProductRanking() {
//        try {
//            Map<String, Object> result = rankingService.getRecommendedProductRanking();
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "추천 상품 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * 세일 상품 랭킹 조회
//     */
//    @GetMapping("/products/sale")
//    public ResponseEntity<?> getSaleProductRanking() {
//        try {
//            Map<String, Object> result = rankingService.getSaleProductRanking();
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "세일 상품 랭킹 조회 중 오류가 발생했습니다: " + e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * 메인 카테고리 목록 조회
//     */
//    @GetMapping("/categories/main")
//    public ResponseEntity<?> getMainCategories() {
//        try {
//            Map<String, Object> result = rankingService.getMainCategories();
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "메인 카테고리 조회 중 오류가 발생했습니다: " + e.getMessage()
//            ));
//        }
//    }
//
//    /**
//     * 하위 카테고리 목록 조회
//     */
//    @GetMapping("/categories/sub")
//    public ResponseEntity<?> getSubCategories(@RequestParam String mainCategory) {
//        try {
//            Map<String, Object> result = rankingService.getSubCategories(mainCategory);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "하위 카테고리 조회 중 오류가 발생했습니다: " + e.getMessage()
//            ));
//        }
//    }
//}