package com.ootd.ootd.model.entity.product;

import com.ootd.ootd.utils.StringToListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)  // 감사 기능 활성화
public class Product {

    @Id
    @Column(name = "product_no", unique = true)
    private Long productNo;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(length = 50 , name = "product_brand")
    private String brandName;

    @Column(nullable = false ,name="product_price")
    private int price;

    @Column(name = "product_image_url", length = 255 , columnDefinition = "TEXT")
    @Convert(converter = StringToListConverter.class)
    private List<String> imageUrls;

    @Column(length = 50 ,name = "product_category")
    private String category;

    @Column(length = 20 ,name = "product_size")
    private String size;

    // 컬렉션 필드 매핑
    @ElementCollection
    @CollectionTable(
            name = "product_colors",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "color", length = 30)
    private List<String> color = new ArrayList<>();

    // 감사 필드
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 낙관적 락 추가 (선택사항)
    @Version
    private Integer version;
}