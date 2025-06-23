package com.ootd.ootd.model.dto.product;

import com.ootd.ootd.model.entity.productOption.ProductOption;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProductOptionDTO {

    private Long optionId;
    private String size;
    private int inventory;
    private String status;
    private Long productNo;

    public static ProductOption convertToEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        return ProductOption.builder()
                .optionId(dto.getProductOption().getOptionId())
                .size(dto.getProductOption().getSize())
                .inventory(dto.getProductOption().getInventory())
                .status(dto.getProductOption().getStatus() != null ? dto.getProductOption().getStatus() : "ACTIVE")
                .productNo(dto.getProductOption().getProductNo())
                .build();
    }
    public static ProductOptionDTO convertToDTO(ProductOption entity) {
        if (entity == null) {
            return null;
        }

        return ProductOptionDTO.builder()
                .optionId(entity.getOptionId())
                .size(entity.getSize())
                .inventory(entity.getInventory())
                .status(entity.getStatus())
                .productNo(entity.getProductNo())
                .build();
    }
}
