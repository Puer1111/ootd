package com.ootd.ootd.service.promotion;

import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.model.entity.promotion.ProductPromotion;

import java.util.List;
import java.util.Map;

public interface ProductPromotionService {

    /**
     * 상품 프로모션 정보 조회
     */
    ProductPromotionDTO getPromotionByProductNo(Long productNo);

    /**
     * 여러 상품의 프로모션 정보 한번에 조회
     */
    Map<Long, ProductPromotionDTO> getPromotionsByProductNos(List<Long> productNos);

    /**
     * 상품 추천 설정/해제
     */
    ProductPromotionDTO setRecommended(Long productNo, boolean isRecommended, Integer priority);

    /**
     * 상품 세일 설정/해제
     */
    ProductPromotionDTO setSale(Long productNo, boolean isSale, Integer salePercentage, Integer originalPrice);

    /**
     * 추천 상품 목록 조회
     */
    List<ProductPromotionDTO> getRecommendedProducts();

    /**
     * 세일 상품 목록 조회
     */
    List<ProductPromotionDTO> getSaleProducts();

    /**
     * 프로모션 정보 저장/수정
     */
    ProductPromotionDTO savePromotion(ProductPromotionDTO promotionDTO);

    /**
     * 프로모션 정보 삭제
     */
    void deletePromotion(Long productNo);

    /**
     * 만료된 세일 정리
     */
    void cleanupExpiredSales();
}