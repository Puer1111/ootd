package com.ootd.ootd.model.entity.cart;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long identificationNo;

    @Column(name="cart_Id")
    String cartId;

    @Column(name="product_No", nullable=false)
    Long productNo;
    @Column(name="product_Name" , nullable=false)
    String productName;
    @Column(name="product_Price", nullable=false)
    int price;
    @Column(name="product_Quantity", nullable=false)
    int quantity;
    @Column(name="product_image_url", nullable=false)
    String imageUrls;

    @Column(name="cart_current_time")
    LocalDateTime currentTime;



}
