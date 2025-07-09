package com.ootd.ootd.model.entity.order;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString
@AllArgsConstructor
@Table(name="orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="orders_id")  // 🔥 소문자로 수정
    private Long orderId;

    // 🆕 사용자 ID 필드 추가 (필요하다면)
    @Column(name="user_id")
    private Long userId;

    @Column(name="orders_quantity")  // 🔥 소문자로 수정
    private Long quantity;

    @CreationTimestamp
    @Column(name="orders_date", nullable=false)  // 🔥 소문자로 수정
    private LocalDateTime orderDate;

    @Column(name="merchant_uid", nullable = false)  // 🔥 소문자로 수정
    private String merchantUid;

    @Column(name="user_name")  // 🔥 소문자로 수정
    private String userName;

    @Column(name="product_name")  // 🔥 소문자로 수정
    private String productName;

    @Column(name="product_price")  // 🔥 소문자로 수정
    private Integer productPrice;

    @Column(name="sale_percent")  // 🔥 소문자로 수정
    private Integer salePercent;

    @Column(name="orders_price")  // 🔥 소문자로 수정
    private Long totalPrice;

    @Column(name="order_status")
    private String orderStatus = "wait";

    // 기존 생성자들...
    public Order(Long orderId, Long quantity, LocalDateTime orderDate, String merchantUid, String userName, String productName, Integer productPrice, Integer salePercent, Long totalPrice, String orderStatus) {
        this.orderId = orderId;
        this.quantity = quantity;
        this.orderDate = orderDate;
        this.merchantUid = merchantUid;
        this.userName = userName;
        this.productName = productName;
        this.productPrice = productPrice;
        this.salePercent = salePercent;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
    }

    // 🆕 userId를 포함한 생성자 (필요하다면)
    public Order(Long userId, Long quantity, String merchantUid, String userName,
                 String productName, Integer productPrice, Integer salePercent,
                 Long totalPrice, String orderStatus) {
        this.userId = userId;
        this.quantity = quantity;
        this.merchantUid = merchantUid;
        this.userName = userName;
        this.productName = productName;
        this.productPrice = productPrice;
        this.salePercent = salePercent;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus != null ? orderStatus : "wait";
    }
}