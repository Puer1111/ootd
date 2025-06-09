package com.ootd.ootd.model.entity.cart;

import com.ootd.ootd.utils.StringToListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class Cart {

    @Id
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
    @Convert(converter = StringToListConverter.class)
    String imageUrls;

//    public String getFirstImageUrl() {
//        // ✅ JSON 파싱 없이 바로 접근
//        if (imageUrls == null || imageUrls.isEmpty()) {
//            return null;  // 또는 기본 이미지 URL
//        }
//
//        return imageUrls.get(0);  // ✅ 첫 번째 이미지 바로 반환
//    }


}
