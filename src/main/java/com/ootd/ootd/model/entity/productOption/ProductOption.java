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

    @Column(name="option_product_size" , nullable = false)
    private String size;

    @Column(name="option_product_inventory", nullable = false)
    private int inventory;

    @Column(name="option_product_status" , nullable = false)
    private String status;

    @Column(name="option_product_color", nullable = false)
    private Long colorNo;

    @Column(name="option_product_no")
    private Long productNo;
}
