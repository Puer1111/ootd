package com.ootd.ootd.model.dto.product;

import com.ootd.ootd.model.entity.product.Product;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Data
public class ProductDetailDTO {
    private Long productNo;
    private String productName;
    private Integer price;
    private String description;
    private String brandName;
    private Long brandNo;
    private List<Integer> inventory;

    public ProductDetailDTO(Long productNo, String productName, Integer price, String description, String brandName, Long brandNo) {
        this.productNo = productNo;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.brandName = brandName;
        this.brandNo = brandNo;
    }

    private int likeCount = 0;
    private int reviewCount = 0;
    private double averageRating = 0.0;


}
