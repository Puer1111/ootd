package com.ootd.ootd.model.dto.promotion;

import com.ootd.ootd.model.entity.promotion.ProductPromotion;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductPromotionDTO {

    private Long productNo;

    // 🔧 기본값 설정으로 null 방지
    private Boolean isRecommended = false;
    private Boolean isSale = false;
    private Integer salePercentage;
    private Integer originalPrice;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;
    private Integer promotionPriority;

    // 계산된 필드들 - 기본값 설정
    private Integer salePrice;
    private Boolean isActiveSale = false;

    public static ProductPromotionDTO convertToDTO(ProductPromotion entity) {
        if (entity == null) {
            return null;
        }

        return ProductPromotionDTO.builder()
                .productNo(entity.getProductNo())
                .isRecommended(entity.getIsRecommended() != null ? entity.getIsRecommended() : false)
                .isSale(entity.getIsSale() != null ? entity.getIsSale() : false)
                .salePercentage(entity.getSalePercentage())
                .originalPrice(entity.getOriginalPrice())
                .saleStartDate(entity.getSaleStartDate())
                .saleEndDate(entity.getSaleEndDate())
                .promotionPriority(entity.getPromotionPriority())
                .salePrice(entity.calculateSalePrice())
                .isActiveSale(entity.isActiveSale()) // primitive boolean이면 null 체크 불필요
                .build();
    }

    public static ProductPromotion convertToEntity(ProductPromotionDTO dto) {
        if (dto == null) {
            return null;
        }

        return ProductPromotion.builder()
                .productNo(dto.getProductNo())
                .isRecommended(dto.getIsRecommended() != null ? dto.getIsRecommended() : false)
                .isSale(dto.getIsSale() != null ? dto.getIsSale() : false)
                .salePercentage(dto.getSalePercentage())
                .originalPrice(dto.getOriginalPrice())
                .saleStartDate(dto.getSaleStartDate())
                .saleEndDate(dto.getSaleEndDate())
                .promotionPriority(dto.getPromotionPriority() != null ? dto.getPromotionPriority() : 0)
                .build();
    }
}