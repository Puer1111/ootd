package com.ootd.ootd.model.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionDetailDTO {
    private Long optionId;
    private String size;
    private int inventory;
    private String status;
    private Long colorNo;
    private String colorName;
}
