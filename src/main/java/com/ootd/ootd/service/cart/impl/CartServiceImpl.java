package com.ootd.ootd.service.cart.impl;

import com.ootd.ootd.model.dto.cart.CartDTO;
import com.ootd.ootd.model.entity.cart.Cart;
import com.ootd.ootd.repository.cart.CartRepository;
import com.ootd.ootd.repository.product.ProductRepository;
import com.ootd.ootd.service.cart.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;


    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CartDTO> getCartItems(String cartId) {
        try {
            List<Cart> result = cartRepository.findByCartId(cartId);
            List<CartDTO> cartDTOList = new ArrayList<>();
            for (Cart cart : result) {
                CartDTO savedCart = CartDTO.convertToDTO(cart);
                cartDTOList.add(savedCart);
            }
            return cartDTOList;
        } catch (Exception e) {
            throw new RuntimeException("cartService-getCartItems is not working :" + e.getMessage());
        }
    }

    @Override
    public void addToCart(CartDTO dto, String cartId) {
        try {
            dto.setCartId(cartId);

            //  장바구니 항목 조회
            Optional<Cart> existingCartOpt = cartRepository.findByCartIdAndProductNo(cartId, dto.getProductNo());

            if (existingCartOpt.isPresent()) {
                //  기존 Entity 수정
                Cart existingCartEntity = existingCartOpt.get(); // get메서드를 통해 Entity 자체를 가져옴.
                int newQuantity = existingCartEntity.getQuantity() + dto.getQuantity();
                existingCartEntity.setQuantity(newQuantity);

                //  Entity 업데이트
                cartRepository.save(existingCartEntity);
                System.out.println("기존 상품 수량 증가: " + dto.getQuantity() + " (수량: " + newQuantity + ")");

            } else {
                Cart newCartEntity = CartDTO.convertToEntity(dto);  // DTO → Entity 변환

                cartRepository.save(newCartEntity);
                System.out.println("새 상품 추가: " + dto.getProductName());
            }

        } catch (Exception e) {
            throw new RuntimeException("장바구니 추가 실패: " + e.getMessage());
        }
    }

}
