package com.ootd.ootd.service.promotion;

import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.model.entity.promotion.ProductPromotion;

import java.util.List;
import java.util.Map;

public interface ProductPromotionService {

    ProductPromotionDTO getPromotionByProductNo(Long productNo);

    Map<Long, ProductPromotionDTO> getPromotionsByProductNos(List<Long> productNos);

    ProductPromotionDTO setRecommended(Long productNo, boolean isRecommended, Integer priority);

    ProductPromotionDTO setSale(Long productNo, boolean isSale, Integer salePercentage, Integer originalPrice);

    List<ProductPromotionDTO> getRecommendedProducts();

    List<ProductPromotionDTO> getSaleProducts();

    ProductPromotionDTO savePromotion(ProductPromotionDTO promotionDTO);

    void deletePromotion(Long productNo);

    void cleanupExpiredSales();
}