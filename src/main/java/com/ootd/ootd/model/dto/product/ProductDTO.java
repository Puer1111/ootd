package com.ootd.ootd.model.dto.product;

import com.ootd.ootd.model.entity.product.Product;
import com.ootd.ootd.model.entity.product_colors.ProductColors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProductDTO {
    private Long productNo;
    private String productName;
    private String brandName;
    private int price;
    private MultipartFile[] images;
    private List<String> imageUrls;
    private String category;
    private String size;
    private List<Long> colorsNo;
    private Long productColorsNo;


    public static ProductDTO convertToDTO(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.setProductNo(entity.getProductNo());
        dto.setProductName(entity.getProductName());
        dto.setBrandName(entity.getBrandName());
        dto.setPrice(entity.getPrice());
        dto.setImageUrls(entity.getImageUrls());
        dto.setCategory(entity.getCategory());
        dto.setSize(entity.getSize());

        ProductColors productColors = entity.getProductColors();
        dto.setProductColorsNo(productColors.getProductColorsNo());
        return dto;
    }

    public static Product convertToEntity(ProductDTO dto) {
        Product entity = new Product();
        entity.setProductNo(dto.getProductNo());
        entity.setProductName(dto.getProductName());
        entity.setBrandName(dto.getBrandName());
        entity.setPrice(dto.getPrice());
        entity.setImageUrls(dto.getImageUrls());
        entity.setCategory(dto.getCategory());
        entity.setSize(dto.getSize());
        ProductColors productColors = new ProductColors();
        productColors.setProductColorsNo(dto.getProductColorsNo());
        entity.setProductColors(productColors);
        return entity;
    }
}
