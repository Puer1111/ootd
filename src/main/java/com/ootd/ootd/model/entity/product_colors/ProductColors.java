package com.ootd.ootd.model.entity.product_colors;

import com.ootd.ootd.model.entity.colors.Colors;
import com.ootd.ootd.model.entity.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="product_colors")
@NoArgsConstructor
public class ProductColors {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_colors_no")
    private Long productColorsNo;

    @ManyToOne
    @JoinColumn(name = "product_no")  // 이 부분이 중요: colors_no 컬럼을 통해 Colors 테이블과 조인
    private Product product;

//    @Column(name = "colors_no")
//    private Long colorNo;

    @ManyToOne
    @JoinColumn(name = "colors_no")  // 이 부분이 중요: colors_no 컬럼을 통해 Colors 테이블과 조인
    private Colors colors;  // 엔티티 관계 필드

    public ProductColors(Colors colors) {
        this.colors = colors;
    }
}
