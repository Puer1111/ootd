package com.ootd.ootd.model.entity.order;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString
@Table(name="orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="orders_Id")
    private Long orderId;

    @Column(name= "orders_Quantity")
    private Long quantity;

    @CreationTimestamp
    @Column(name="orders_Date" , nullable=false)
    private String orderDate;

    @Column(name="merchant_Uid" , nullable = false)
    private String merchantUid;

    @Column(name="user_Name")
    private String userName;

    @Column(name="product_Name")
    private String productName;

    @Column(name="product_Price")
    private Integer productPrice;

    @Column(name="salePercent")
    private Integer salePercent;

    @Column(name="orders_Price")
    private Long totalPrice;

    @Column(name="order_status")
    private String orderStatus = "wait";

    public Order(Long orderId, Long quantity, String orderDate, String merchantUid, String userName, String productName, Integer productPrice, Integer salePercent, Long totalPrice, String orderStatus) {
        this.orderId = orderId;
        this.quantity = quantity;
        this.orderDate = orderDate;
        this.merchantUid = merchantUid;
        this.userName = userName;
        this.productName = productName;
        this.productPrice = productPrice;
        this.salePercent = salePercent;
        this.totalPrice = totalPrice;
        this.orderStatus = "wait";
    }
}
