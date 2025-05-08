package com.ootd.ootd.model.dto.product;

import com.ootd.ootd.model.entity.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProductDTO {
    private Integer productNo;
    private String productName;
    private String brand;
    private int price;
    private String imageUrl;
    private String category;
    private String size;
    private List<String> color;

    public ProductDTO(Integer productNo, String productName, String brand, int price, String imageUrl, String category, String size, List<String> color) {
        this.productNo = productNo;
        this.productName = productName;
        this.brand = brand;
        this.price = price;
        this.imageUrl = imageUrl;
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
        dto.setImageUrl(entity.getImageUrl());
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
        entity.setImageUrl(dto.getImageUrl());
        entity.setCategory(dto.getCategory());
        entity.setSize(dto.getSize());
        entity.setColor(dto.getColor());
        return entity;
    }
}
