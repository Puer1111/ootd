package com.ootd.ootd.model.dto.product;

import com.ootd.ootd.model.entity.product.Product;
import com.ootd.ootd.model.entity.product_colors.ProductColors;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
@Data
public class ProductDTO {
    private Long productNo;
    private String productName;
    private Integer price;
    private String description;
    private String brandName;
    private Long brandNo;
    private MultipartFile[] images;
    private List<String> imageUrls;
    private Long categoryNo;
    private String subCategory;
    private Long subCategoryNo;
    private List<Long> colorsNo;
    private Long productColorsNo;


    public ProductDTO(Long productNo, String productName, Integer price, String description, String brandName, Long brandNo, List<String> imageUrls, Long categoryNo,String subCategory) {
        this.productNo = productNo;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.brandName = brandName;
        this.brandNo = brandNo;
        this.imageUrls = imageUrls;
        this.categoryNo = categoryNo;
        this.subCategory = subCategory;
    }

    // 좋아요 리뷰
    private int likeCount = 0;
    private int reviewCount = 0;
    private double averageRating = 0.0;


    public static ProductDTO convertToDTO(Product entity) {
        return ProductDTO.builder()
                .productNo(entity.getProductNo())
                .productName(entity.getProductName())
                .brandNo(entity.getBrandNo())
                .price(entity.getPrice())
                .imageUrls(entity.getImageUrls())
                .categoryNo(entity.getCategoryNo())
                .description(entity.getDescription())
                .build();
    }

    public static Product convertToEntity(ProductDTO dto) {
    return Product.builder()
                .productNo(dto.getProductNo())
                .productName(dto.getProductName())
                .brandNo(dto.getBrandNo())
                .price(dto.getPrice())
                .imageUrls(dto.getImageUrls())
                .categoryNo(dto.getCategoryNo())
                .description(dto.getDescription())
                .build();

    }
}
