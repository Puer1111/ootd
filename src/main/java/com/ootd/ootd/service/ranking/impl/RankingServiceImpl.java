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

        if (subCategory != null && !subCategory.trim().isEmpty() && !"all".equals(subCategory)) {
            // 하위 카테고리로 필터링
            products = productRepository.findBySubCategoryOrderByReviewCountDesc(subCategory);
        } else if (mainCategory != null && !mainCategory.trim().isEmpty() && !"all".equals(mainCategory)) {
            // 메인 카테고리로 필터링
            products = productRepository.findByMainCategoryOrderByReviewCountDesc(mainCategory);
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
        List<String> categories = productRepository.findSubCategoriesByMainCategory(mainCategory);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("categories", categories);
        result.put("mainCategory", mainCategory);

        return result;
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