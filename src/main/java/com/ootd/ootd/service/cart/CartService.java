package com.ootd.ootd.service.cart;

import com.ootd.ootd.model.dto.cart.CartDTO;

import java.util.List;

public interface CartService {
    List<CartDTO> getCartItems(String cartId);

    void addToCart(CartDTO dto, String cartId);
}
