package com.ootd.ootd.model.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseInfoDTO {
    private Long productNo;
    private String productName;
    private String description;
    private Integer price;
    private Long brandNo;
    private Long categoryNo;
    private Boolean isActiveSale;
    private Integer salePercentage;
    private MultipartFile[] images;
    private List<String> imageUrls;


    private List<ProductOptionDTO> options;
}