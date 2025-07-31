package com.ootd.ootd.model.entity.order;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name="orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="orders_id")
    private Long orderId;

    @Column(name="user_id")
    private Long userId;

    @Column(name="orders_quantity")
    private Long quantity;

    @CreationTimestamp
    @Column(name="orders_date", nullable=false)
    private LocalDateTime orderDate;

    @Column(name="merchant_uid", nullable = false)
    private String merchantUid;

    @Column(name="user_name")
    private String userName;

    @Column(name="product_name")
    private String productName;

    @Column(name="product_price")
    private Integer productPrice;

    @Column(name="sale_percent")
    private Integer salePercent;

    @Column(name="orders_price")
    private Long totalPrice;

    @Column(name="order_status")
    private String orderStatus = "wait";
    @PrePersist
    public void onPrePersist() {
        if (this.orderStatus == null) {
            this.orderStatus = "wait";
        }
    }

    @Column(name = "imp_uid", length = 100)
    private String impUid;

    public Order(Long userId, Long quantity, String merchantUid, String userName,
                 String productName, Integer productPrice, Integer salePercent,
                 Long totalPrice, String orderStatus, String impUid) {
        this.userId = userId;
        this.quantity = quantity;
        this.merchantUid = merchantUid;
        this.userName = userName;
        this.productName = productName;
        this.productPrice = productPrice;
        this.salePercent = salePercent;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus != null ? orderStatus : "wait";
        this.impUid = impUid;
    }
}