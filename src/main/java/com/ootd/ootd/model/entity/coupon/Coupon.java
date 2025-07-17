package com.ootd.ootd.model.entity.coupon;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;      // 쿠폰 분류 ID

    @Column(name = "coupon_name")
    private String couponName;      // 쿠폰 이름

    @Column(name = "category_id")
    private Long categoryId;        // 쿠폰 <-> 카테고리

    @Column(name = "discount_rate")
    private Integer discountRate;       // 할인율

    private Integer quantity;           // 개수

    @Column(name = "expiration_date")
    private LocalDate expirationDate;       // 만료 날짜

    @Column(name = "receive_limit")
    private Integer receiveLimit;           // 수량 한도

    @Column(name = "usage_limit")
    private Integer usageLimit;         // 사용 한도
}
