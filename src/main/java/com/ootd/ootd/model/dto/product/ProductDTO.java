package com.ootd.ootd.model.dto.product;

import com.ootd.ootd.model.entity.product.Product;
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
    private String brand;
    private int price;
    private MultipartFile image;
    private List<String> imageUrls;
    private String category;
    private String size;
    private List<String> color;

    public ProductDTO(Long productNo, String productName, String brand, int price, MultipartFile image, List<String> imageUrls, String category, String size, List<String> color) {
        this.productNo = productNo;
        this.productName = productName;
        this.brand = brand;
        this.price = price;
        this.image = image;
        this.imageUrls = imageUrls;
        this.category = category;
        this.size = size;
        this.color = color;
    }

    public static ProductDTO convertToDTO(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.setProductNo(entity.getProductNo());
        dto.setProductName(entity.getProductName());
        dto.setBrand(entity.getBrand());
        dto.setPrice(entity.getPrice());
        dto.setImageUrls(entity.getImageUrls());
        dto.setCategory(entity.getCategory());
        dto.setSize(entity.getSize());
        dto.setColor(entity.getColor());
        return dto;
    }

    public static List<ProductDTO> fromEntityList(List<Product> entities) {
        return entities.stream()
                .map(ProductDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    public static Product convertToEntity(ProductDTO dto) {
        Product entity = new Product();
        entity.setProductNo(dto.getProductNo());
        entity.setProductName(dto.getProductName());
        entity.setBrand(dto.getBrand());
        entity.setPrice(dto.getPrice());
        entity.setImageUrls(dto.getImageUrls());
        entity.setCategory(dto.getCategory());
        entity.setSize(dto.getSize());
        entity.setColor(dto.getColor());
        return entity;
    }
}
