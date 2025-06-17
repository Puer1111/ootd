package com.ootd.ootd.model.entity.productOption;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="option_Id")
    private Long optionId;

    @Column(name="product_size" , nullable = false)
    private String size;

    @Column(name="product_inventory", nullable = false)
    private int inventory;

    @Column(name="product_status" , nullable = false)
    private String status;

    @Column(name="product_productNo")
    private Long productNo;
}
