package com.ootd.ootd.model.entity.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.ootd.ootd.model.entity.product_colors.ProductColors;
import com.ootd.ootd.utils.StringToListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "product")
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)  // 감사 기능 활성화
@Builder
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_no", unique = true)
    private Long productNo;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(length = 50 , name = "product_brand")
    private Long brandNo;

    @Column(nullable = false ,name="product_price")
    private Integer price;

    @Column(name = "product_image_url", length = 500 , columnDefinition = "TEXT")
    @Convert(converter = StringToListConverter.class)
    private List<String> imageUrls;

    @Column(length = 50 ,name = "product_category")
    private Long categoryNo;

    @Column(name="product_description")
    private String description;

//    @ManyToOne
//    @JoinColumn(name = "product_colors_no")
//    private ProductColors productColors;


}