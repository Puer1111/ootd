package com.ootd.ootd.service.promotion.impl;

import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.model.entity.promotion.ProductPromotion;
import com.ootd.ootd.repository.promotion.ProductPromotionRepository;
import com.ootd.ootd.service.promotion.ProductPromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductPromotionServiceImpl implements ProductPromotionService {

    @Autowired
    private ProductPromotionRepository promotionRepository;

    @Override
    public ProductPromotionDTO getPromotionByProductNo(Long productNo) {
        return promotionRepository.findByProductNo(productNo)
                .map(ProductPromotionDTO::convertToDTO)
                .orElse(null);
    }

    @Override
    public Map<Long, ProductPromotionDTO> getPromotionsByProductNos(List<Long> productNos) {
        List<ProductPromotion> promotions = promotionRepository.findByProductNoIn(productNos);

        return promotions.stream()
                .collect(Collectors.toMap(
                        ProductPromotion::getProductNo,
                        ProductPromotionDTO::convertToDTO
                ));
    }

    @Override
    public ProductPromotionDTO setRecommended(Long productNo, boolean isRecommended, Integer priority) {
        ProductPromotion promotion = promotionRepository.findByProductNo(productNo)
                .orElse(ProductPromotion.builder()
                        .productNo(productNo)
                        .build());

        promotion.setIsRecommended(isRecommended);
        if (isRecommended && priority != null) {
            promotion.setPromotionPriority(priority);
        }

        ProductPromotion saved = promotionRepository.save(promotion);
        return ProductPromotionDTO.convertToDTO(saved);
    }

    @Override
    public ProductPromotionDTO setSale(Long productNo, boolean isSale, Integer salePercentage, Integer originalPrice) {
        ProductPromotion promotion = promotionRepository.findByProductNo(productNo)
                .orElse(ProductPromotion.builder()
                        .productNo(productNo)
                        .build());

        promotion.setIsSale(isSale);

        if (isSale) {
            promotion.setSalePercentage(salePercentage);
            promotion.setOriginalPrice(originalPrice);
            // 세일 시작 시간을 현재로 설정 (종료 시간은 별도 설정)
            if (promotion.getSaleStartDate() == null) {
                promotion.setSaleStartDate(LocalDateTime.now());
            }
        } else {
            // 세일 해제 시 관련 정보 초기화
            promotion.setSalePercentage(null);
            promotion.setSaleStartDate(null);
            promotion.setSaleEndDate(null);
        }

        ProductPromotion saved = promotionRepository.save(promotion);
        return ProductPromotionDTO.convertToDTO(saved);
    }

    @Override
    public List<ProductPromotionDTO> getRecommendedProducts() {
        return promotionRepository.findByIsRecommendedTrueOrderByPromotionPriorityDesc()
                .stream()
                .map(ProductPromotionDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductPromotionDTO> getSaleProducts() {
        return promotionRepository.findActiveSaleProducts(LocalDateTime.now())
                .stream()
                .map(ProductPromotionDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductPromotionDTO savePromotion(ProductPromotionDTO promotionDTO) {
        ProductPromotion entity = ProductPromotionDTO.convertToEntity(promotionDTO);
        ProductPromotion saved = promotionRepository.save(entity);
        return ProductPromotionDTO.convertToDTO(saved);
    }

    @Override
    public void deletePromotion(Long productNo) {
        promotionRepository.deleteById(productNo);
    }

    @Override
    public void cleanupExpiredSales() {
        List<ProductPromotion> expiredSales = promotionRepository.findExpiredSaleProducts(LocalDateTime.now());

        for (ProductPromotion promotion : expiredSales) {
            promotion.setIsSale(false);
            promotion.setSalePercentage(null);
            promotion.setSaleStartDate(null);
            promotion.setSaleEndDate(null);
        }

        promotionRepository.saveAll(expiredSales);

        System.out.println("만료된 세일 " + expiredSales.size() + "개 정리 완료");
    }
}