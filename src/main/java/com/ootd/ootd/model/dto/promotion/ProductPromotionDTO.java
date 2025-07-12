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
    private Boolean isRecommended;
    private Boolean isSale;
    private Integer salePercentage;
    private Integer originalPrice;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;
    private Integer promotionPriority;

    // 계산된 필드들
    private Integer salePrice;
    private Boolean isActiveSale;

    public static ProductPromotionDTO convertToDTO(ProductPromotion entity) {
        if (entity == null) {
            return null;
        }

        return ProductPromotionDTO.builder()
                .productNo(entity.getProductNo())
                .isRecommended(entity.getIsRecommended())
                .isSale(entity.getIsSale())
                .salePercentage(entity.getSalePercentage())
                .originalPrice(entity.getOriginalPrice())
                .saleStartDate(entity.getSaleStartDate())
                .saleEndDate(entity.getSaleEndDate())
                .promotionPriority(entity.getPromotionPriority())
                .salePrice(entity.calculateSalePrice())
                .isActiveSale(entity.isActiveSale())
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