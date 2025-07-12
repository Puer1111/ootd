package com.ootd.ootd.model.entity.promotion;

import com.ootd.ootd.model.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_promotion")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductPromotion {

    @Id
    @Column(name = "product_no")
    private Long productNo;

    @Column(name = "is_recommended", nullable = false)
    @Builder.Default
    private Boolean isRecommended = false;

    @Column(name = "is_sale", nullable = false)
    @Builder.Default
    private Boolean isSale = false;

    @Column(name = "sale_percentage")
    private Integer salePercentage; // 할인율 (0-100)

    @Column(name = "original_price")
    private Integer originalPrice; // 원래 가격 (세일 시 비교용)

    @Column(name = "sale_start_date")
    private LocalDateTime saleStartDate;

    @Column(name = "sale_end_date")
    private LocalDateTime saleEndDate;

    @Column(name = "promotion_priority")
    @Builder.Default
    private Integer promotionPriority = 0; // 추천 우선순위 (높을수록 우선)

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_no", insertable = false, updatable = false)
    private Product product;

    // 세일 가격 계산 메서드
    public Integer calculateSalePrice() {
        if (isSale && salePercentage != null && originalPrice != null) {
            return originalPrice - (originalPrice * salePercentage / 100);
        }
        return originalPrice;
    }

    // 세일 진행 중인지 확인
    public boolean isActiveSale() {
        if (!isSale) return false;

        LocalDateTime now = LocalDateTime.now();
        if (saleStartDate != null && now.isBefore(saleStartDate)) return false;
        if (saleEndDate != null && now.isAfter(saleEndDate)) return false;

        return true;
    }
}