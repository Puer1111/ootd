package com.ootd.ootd.model.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductFlatDTO {
    private Long productNo;
    private String productName;
    private Integer price;
    private String description;
    private List<String> imageUrls;
    // brand
    private String brandName;
    // category
    private Long categoryNo;
    private String subCategory;
    //productOption
    private String size;
    private Integer inventory;
    private String status;
    private String colorName;
    private Long colorsNo;

}
