package com.ootd.ootd.model.entity.product_colors;

import com.ootd.ootd.utils.LongListToStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    @Column(name = "colors_no",length = 500)
    @Convert(converter = LongListToStringConverter.class)
    private List<Long> colorsNo;  // 여러 색상 번호를 리스트로 저장

    public ProductColors(List<Long> colorsNo) {
        this.colorsNo = colorsNo;
    }
}
