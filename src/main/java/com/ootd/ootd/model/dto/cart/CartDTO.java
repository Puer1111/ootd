package com.ootd.ootd.model.dto.cart;

import com.ootd.ootd.model.entity.cart.Cart;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class CartDTO {
    String cartId;
    Long productNo;
    String productName;
    int price;
    int quantity;
    String imageUrls;

    public static CartDTO convertToDTO(Cart cart) {
       return CartDTO.builder().cartId(cart.getCartId()).productNo(cart.getProductNo()).productName(cart.getProductName())
                .price(cart.getPrice()).quantity(cart.getQuantity()).imageUrls(cart.getImageUrls()).build();
    }
    public static Cart convertToEntity(CartDTO dto) {
        return Cart.builder().cartId(dto.getCartId()).productNo(dto.getProductNo()).productName(dto.getProductName()).price(dto.getPrice()).quantity(dto.getQuantity()).imageUrls(dto.getImageUrls())
                .build();
    }
}

