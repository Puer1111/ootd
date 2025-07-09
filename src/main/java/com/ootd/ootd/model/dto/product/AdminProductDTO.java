package com.ootd.ootd.model.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductDTO {
    private Long productNo;
    private String productName;
    private Integer price;
    private String description;
    private String brandName;
    private Long categoryNo;
    private String subCategory;
    private List<ProductOptionInfo> options;
    private List<String> imageUrls;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductOptionInfo {
        private String size;
        private Integer inventory;
        private String status;
        private String colorName;
    }
}
