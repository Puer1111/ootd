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
    private Long couponId;

    @Column(name = "coupon_name")
    private String couponName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "discount_rate")
    private Integer discountRate;

    private Integer quantity;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "receive_limit")
    private Integer receiveLimit;

    @Column(name = "usage_limit")
    private Integer usageLimit;
}
