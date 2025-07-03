package com.ootd.ootd.model.entity.colors;

import com.ootd.ootd.model.entity.product_colors.ProductColors;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="colors")
public class Colors {
    @Id
    @Column(name="colors_no" , length = 255 , nullable = false)
    private Long colorsNo;

    @Column(name ="colors_name" , nullable = false, unique = true)
    private String colorName;
}
//
//    @ManyToOne
//    @JoinColumn(name="product_colors_no")
//    private ProductColors productColors;
