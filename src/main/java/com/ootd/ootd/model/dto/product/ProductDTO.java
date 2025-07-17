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
    private String mainCategory; // 메인 카테고리 추가
    private Long productColorsNo;

    private ProductOptionDTO productOption;

    // 좋아요 리뷰
    private int likeCount = 0;
    private int reviewCount = 0;
    private double averageRating = 0.0;

    // 🆕 추천/세일 정보 추가
    private ProductPromotionDTO promotion;

    // 편의 메서드들
    private Boolean isRecommended = false;
    private Boolean isSale = false;
    private Integer salePercentage;
    private Integer salePrice;
    private Boolean isActiveSale = false;

    // 기존 생성자
    public ProductDTO(Long productNo, String productName, Integer price, String description,
                      String brandName, Long brandNo, List<String> imageUrls, Long categoryNo, String subCategory) {
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

    // 🆕 확장된 생성자 (메인 카테고리 포함)
    public ProductDTO(Long productNo, String productName, Integer price, String description,
                      String brandName, Long brandNo, List<String> imageUrls, Long categoryNo,
                      String subCategory, String mainCategory) {
        this(productNo, productName, price, description, brandName, brandNo, imageUrls, categoryNo, subCategory);
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

    // 🆕 프로모션 정보 설정 메서드 (개선된 버전)
    public void setPromotionInfo(ProductPromotionDTO promotion) {
        this.promotion = promotion;
        if (promotion != null) {
            this.isRecommended = promotion.getIsRecommended();
            this.isSale = promotion.getIsSale();
            this.salePercentage = promotion.getSalePercentage();
            this.isActiveSale = promotion.getIsActiveSale();

            // 세일 가격 설정 (프로모션에서 가져오거나 퍼센티지로 계산)
            if (promotion.getSalePrice() != null) {
                this.salePrice = promotion.getSalePrice();
            } else if (promotion.getSalePercentage() != null && this.price != null) {
                this.salePrice = calculateSalePriceFromPercentage();
            }
        }
    }

    // 🔴 이 부분만 수정됨 - setSalePercentage 메서드
    public void setSalePercentage(Integer salePercentage) {
        this.salePercentage = salePercentage;
        // 세일 가격 자동 계산
        if (this.price != null && salePercentage != null && salePercentage > 0) {
            this.salePrice = this.price - (this.price * salePercentage / 100);
        }
    }

    // 🆕 실제 표시할 가격 계산 (세일 중이면 세일 가격, 아니면 원래 가격)
    public Integer getDisplayPrice() {
        if (isActiveSale && salePrice != null) {
            return salePrice;
        }
        return price;
    }

    // 🆕 최종 표시 가격 계산 (getTotalPrice와 동일, sale.js 호환용)
    public Integer getTotalPrice() {
        if (isActiveSale != null && isActiveSale && salePrice != null) {
            return salePrice;
        }
        return price;
    }

    // 🆕 할인 금액 계산
    public Integer getSavingsAmount() {
        if (isActiveSale != null && isActiveSale && salePrice != null && price != null) {
            return price - salePrice;
        }
        return 0;
    }

    // 🆕 실제 할인율 계산 (소수점 1자리)
    public Double getActualSalePercentage() {
        if (price != null && salePrice != null && price > 0) {
            double percentage = ((double)(price - salePrice) / price) * 100;
            return Math.round(percentage * 10.0) / 10.0;
        }
        return 0.0;
    }

    // 🆕 세일 가격이 설정되지 않은 경우 퍼센티지로 계산
    public Integer calculateSalePriceFromPercentage() {
        if (salePercentage != null && price != null) {
            return price - (price * salePercentage / 100);
        }
        return price;
    }

    // 🆕 세일 여부 확인 (편의 메서드)
    public boolean isOnSale() {
        return isActiveSale != null && isActiveSale &&
                ((salePrice != null && salePrice < price) ||
                        (salePercentage != null && salePercentage > 0));
    }
}