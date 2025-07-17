package com.ootd.ootd.service.ranking.impl;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.repository.product.ProductRepository;
import com.ootd.ootd.repository.product.ProductLikeRepository;
import com.ootd.ootd.repository.product.ProductReviewRepository;
import com.ootd.ootd.repository.promotion.ProductPromotionRepository;
import com.ootd.ootd.service.ranking.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductPromotionRepository promotionRepository;

    @Override
    public Map<String, Object> getProductRanking() {
        // 기존 ProductRepository의 메서드 활용
        List<ProductDTO> products = productRepository.findAllOrderByReviewCountDesc();

        // 각 상품에 좋아요 수, 리뷰 수, 평점 정보 추가
        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("sortType", "reviews");

        return result;
    }

    @Override
    public Map<String, Object> getProductRankingByCategory(String mainCategory, String subCategory) {
        List<ProductDTO> products;

        // 🆕 카테고리 매핑 적용
        String mappedMainCategory = mapCategoryToKorean(mainCategory);

        if (subCategory != null && !subCategory.trim().isEmpty() && !"all".equals(subCategory)) {
            // 하위 카테고리로 필터링
            products = productRepository.findBySubCategoryOrderByReviewCountDesc(subCategory);
        } else if (mappedMainCategory != null && !mappedMainCategory.trim().isEmpty() && !"all".equals(mappedMainCategory)) {
            // 메인 카테고리로 필터링
            products = productRepository.findByMainCategoryOrderByReviewCountDesc(mappedMainCategory);
        } else {
            // 전체 랭킹
            products = productRepository.findAllOrderByReviewCountDesc();
        }

        // 각 상품에 통계 정보 추가
        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("filter", Map.of(
                "mainCategory", mainCategory != null ? mainCategory : "all",
                "subCategory", subCategory != null ? subCategory : "all"
        ));

        return result;
    }

    @Override
    public Map<String, Object> getRecommendedProductRanking() {
        List<ProductDTO> products = productRepository.findRecommendedProducts();

        // 각 상품에 통계 정보 추가
        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("type", "recommended");

        return result;
    }

    @Override
    public Map<String, Object> getSaleProductRanking() {
        List<ProductDTO> products = productRepository.findSaleProducts();

        // 각 상품에 통계 정보 추가
        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("type", "sale");

        return result;
    }

    @Override
    public Map<String, Object> getMainCategories() {
        List<String> categories = productRepository.findDistinctMainCategories();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("categories", categories);

        return result;
    }

    @Override
    public Map<String, Object> getSubCategories(String mainCategory) {
        // 🆕 카테고리 매핑 적용
        String mappedCategory = mapCategoryToKorean(mainCategory);
        List<String> categories = productRepository.findSubCategoriesByMainCategory(mappedCategory);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("categories", categories);
        result.put("mainCategory", mainCategory);

        return result;
    }

    // ========== 🆕 추가 메서드들 (RankController에서 사용) ==========

    /**
     * 🆕 정렬 기준별 상품 랭킹 조회 (통합 메서드)
     */
    public Map<String, Object> getProductRankingBySortType(String sortBy) {
        List<ProductDTO> products;

        switch (sortBy.toLowerCase()) {
            case "likes":
                products = productRepository.findAllOrderByLikeCountDesc();
                break;
            case "rating":
                products = productRepository.findAllOrderByRatingDesc();
                break;
            case "reviews":
            default:
                products = productRepository.findAllOrderByReviewCountDesc();
                break;
        }

        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("sortType", sortBy);

        return result;
    }

    /**
     * 🆕 카테고리별 + 정렬 기준별 상품 랭킹 조회 (통합 메서드)
     */
    public Map<String, Object> getProductRankingByCategoryAndSortType(String mainCategory, String subCategory, String sortBy) {
        List<ProductDTO> products = getProductsByCategory(mainCategory, subCategory, sortBy);

        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("sortType", sortBy);
        result.put("filter", Map.of(
                "mainCategory", mainCategory != null ? mainCategory : "all",
                "subCategory", subCategory != null ? subCategory : "all"
        ));

        return result;
    }

    // ========== 🆕 헬퍼 메서드들 ==========

    /**
     * 카테고리와 정렬 기준에 따라 상품 목록을 조회하는 헬퍼 메서드
     */
    private List<ProductDTO> getProductsByCategory(String mainCategory, String subCategory, String sortType) {
        // 카테고리 매핑 적용
        String mappedMainCategory = mapCategoryToKorean(mainCategory);

        if (subCategory != null && !subCategory.trim().isEmpty() && !"all".equals(subCategory)) {
            // 하위 카테고리로 필터링
            switch (sortType.toLowerCase()) {
                case "likes":
                    return productRepository.findBySubCategoryOrderByLikeCountDesc(subCategory);
                case "rating":
                    return productRepository.findBySubCategoryOrderByRatingDesc(subCategory);
                case "reviews":
                default:
                    return productRepository.findBySubCategoryOrderByReviewCountDesc(subCategory);
            }
        } else if (mappedMainCategory != null && !mappedMainCategory.trim().isEmpty() && !"all".equals(mappedMainCategory)) {
            // 메인 카테고리로 필터링
            switch (sortType.toLowerCase()) {
                case "likes":
                    return productRepository.findByMainCategoryOrderByLikeCountDesc(mappedMainCategory);
                case "rating":
                    return productRepository.findByMainCategoryOrderByRatingDesc(mappedMainCategory);
                case "reviews":
                default:
                    return productRepository.findByMainCategoryOrderByReviewCountDesc(mappedMainCategory);
            }
        } else {
            // 전체 상품
            switch (sortType.toLowerCase()) {
                case "likes":
                    return productRepository.findAllOrderByLikeCountDesc();
                case "rating":
                    return productRepository.findAllOrderByRatingDesc();
                case "reviews":
                default:
                    return productRepository.findAllOrderByReviewCountDesc();
            }
        }
    }

    /**
     * 영어 카테고리를 한국어 카테고리로 매핑
     */
    private String mapCategoryToKorean(String englishCategory) {
        if (englishCategory == null || "all".equals(englishCategory)) {
            return null;
        }

        switch (englishCategory.toLowerCase()) {
            case "top":
                return "상의";
            case "bottom":
                return "하의";
            case "shoes":
                return "신발";
            default:
                return englishCategory; // 이미 한국어거나 다른 값인 경우 그대로 반환
        }
    }

    /**
     * 상품에 좋아요 수, 리뷰 수, 평점, 프로모션 정보 등을 추가하는 메서드
     */
    private ProductDTO enrichProductWithStats(ProductDTO product) {
        Long productNo = product.getProductNo();

        // 좋아요 수 설정
        int likeCount = productLikeRepository.countByProductNo(productNo);
        product.setLikeCount(likeCount);

        // 리뷰 수 설정
        int reviewCount = productReviewRepository.countByProductNo(productNo);
        product.setReviewCount(reviewCount);

        // 평점 설정
        Double averageRating = productReviewRepository.findAverageRatingByProductNo(productNo);
        product.setAverageRating(averageRating != null ? averageRating : 0.0);

        // 프로모션 정보 설정
        promotionRepository.findByProductNo(productNo).ifPresent(promotion -> {
            ProductPromotionDTO promotionDTO = ProductPromotionDTO.convertToDTO(promotion);
            product.setPromotionInfo(promotionDTO);
        });

        return product;
    }
}