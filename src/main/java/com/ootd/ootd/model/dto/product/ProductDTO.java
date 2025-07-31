package com.ootd.ootd.model.dto.product;

import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.model.entity.product.Product;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private String mainCategory;
    private Long productColorsNo;

    private List<ProductOptionDTO> productOption;

    private int likeCount = 0;
    private int reviewCount = 0;
    private double averageRating = 0.0;

    private ProductPromotionDTO promotion;

    private Boolean isRecommended = false;
    private Boolean isSale = false;
    private Integer salePrice;
    private Integer salePercentage;
    private Boolean isActiveSale = false;

    public ProductDTO(Long productNo, String productName, Integer price, String description,
                      String brandName, Long brandNo, List<String> imageUrls, Long categoryNo, String subCategory,String mainCategory) {
        this.productNo = productNo;
        this.productName = productName;
        this.price = price;
        this.description = description;
        this.brandName = brandName;
        this.brandNo = brandNo;
        this.imageUrls = imageUrls;
        this.categoryNo = categoryNo;
        this.subCategory = subCategory;
        this.mainCategory = mainCategory;
    }

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

    public void setPromotionInfo(ProductPromotionDTO promotion) {
        this.promotion = promotion;
        if (promotion != null) {
            this.isRecommended = promotion.getIsRecommended();
            this.isSale = promotion.getIsSale();
            this.salePercentage = promotion.getSalePercentage();
            this.isActiveSale = promotion.getIsActiveSale();

            if (promotion.getSalePrice() != null) {
                this.salePrice = promotion.getSalePrice();
            } else if (promotion.getSalePercentage() != null && this.price != null) {
                this.salePrice = calculateSalePriceFromPercentage();
            }
        }
    }

    public void setSalePercentage(Integer salePercentage) {
        this.salePercentage = salePercentage;
        if (this.price != null && salePercentage != null && salePercentage > 0) {
            this.salePrice = this.price - (this.price * salePercentage / 100);
        }
    }

    public Integer getDisplayPrice() {
        if (isActiveSale && salePrice != null) {
            return salePrice;
        }
        return price;
    }

    public Integer getTotalPrice() {
        if (isActiveSale != null && isActiveSale && salePrice != null) {
            return salePrice;
        }
        return price;
    }

    public Integer getSavingsAmount() {
        if (isActiveSale != null && isActiveSale && salePrice != null && price != null) {
            return price - salePrice;
        }
        return 0;
    }

    public Double getActualSalePercentage() {
        if (price != null && salePrice != null && price > 0) {
            double percentage = ((double)(price - salePrice) / price) * 100;
            return Math.round(percentage * 10.0) / 10.0;
        }
        return 0.0;
    }

    public Integer calculateSalePriceFromPercentage() {
        if (salePercentage != null && price != null) {
            return price - (price * salePercentage / 100);
        }
        return price;
    }

    public boolean isOnSale() {
        return isActiveSale != null && isActiveSale &&
                ((salePrice != null && salePrice < price) ||
                        (salePercentage != null && salePercentage > 0));
    }
}