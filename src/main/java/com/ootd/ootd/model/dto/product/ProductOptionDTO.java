package com.ootd.ootd.model.dto.product;

import com.ootd.ootd.model.entity.productOption.ProductOption;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProductOptionDTO {

    private Long optionId;
    private List<String> size;
    private List<Integer> inventory;
    private List<String> status;
    private List<Long> productNo;
    private List<Long> colorsNo;

    public static List<ProductOption> convertToEntityList(ProductOptionDTO dto,Long productId) {
        if (dto == null || dto.getSize() == null || dto.getSize().isEmpty()) {
            return new ArrayList<>();
        }

        List<ProductOption> productOptions = new ArrayList<>();

        for (int i = 0; i < dto.getSize().size(); i++) {
            ProductOption option = ProductOption.builder()
                    .size(dto.getSize().get(i))
                    .inventory(dto.getInventory() != null && i < dto.getInventory().size()
                            ? dto.getInventory().get(i) : 0)  // Integer → int 자동 변환
                    .status(dto.getStatus() != null && i < dto.getStatus().size()
                            ? dto.getStatus().get(i) : "ACTIVE")
                    .colorNo(dto.getColorsNo().get(i))
                    .productNo(productId)
                    .build();

            productOptions.add(option);
        }

        return productOptions;
    }

    public static ProductOption convertToEntity(ProductDTO dto) {
        if (dto == null || dto.getProductOption() == null) {
            return null;
        }

        ProductOptionDTO optionDTO = dto.getProductOption();

        return ProductOption.builder()
                .optionId(optionDTO.getOptionId())
                // List에서 첫 번째 값만 가져오기
                .size(optionDTO.getSize() != null && !optionDTO.getSize().isEmpty()
                        ? optionDTO.getSize().getFirst() : null)
                .inventory(optionDTO.getInventory() != null && !optionDTO.getInventory().isEmpty()
                        ? optionDTO.getInventory().getFirst() : 0)
                .status(optionDTO.getStatus() != null && !optionDTO.getStatus().isEmpty()
                        ? optionDTO.getStatus().getFirst() : "ACTIVE")
                .productNo(optionDTO.getProductNo() != null && !optionDTO.getProductNo().isEmpty()
                        ? optionDTO.getProductNo().getFirst() : null)
                .build();
    }
    public static ProductOptionDTO convertToDTO(ProductOption entity) {
        if (entity == null) {
            return null;
        }
        return ProductOptionDTO.builder()
                .optionId(entity.getOptionId())
                .size(List.of(entity.getSize()))
                .inventory(List.of(entity.getInventory()))
                .status(List.of(entity.getStatus()))
                .colorsNo(List.of(entity.getColorNo()))
                .productNo(List.of(entity.getProductNo()))
                .build();
    }
}
