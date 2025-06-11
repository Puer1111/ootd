package com.ootd.ootd.controller.cart;

import com.ootd.ootd.model.dto.cart.CartDTO;
import com.ootd.ootd.model.entity.cart.Cart;
import com.ootd.ootd.service.cart.CartService;
import com.ootd.ootd.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String showCartPage(HttpServletRequest request, Model model) {
        String cartId = CookieUtils.getCookieValue(request, "CART_ID");
        List<CartDTO> cartItems = cartService.getCartItems(cartId); // DB 조회
        System.out.println("Check CartItems : " + cartItems);
        model.addAttribute("Cart", cartItems);
        return "view/cart/cart";

    }

    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@RequestBody CartDTO dto,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        System.out.println("Check For CartDTO : " + dto);
        // 기존 쿠키 확인
        String cartId = CookieUtils.getCookieValue(request, "CART_ID");

        // 쿠키가 없으면 새로 생성
        if (cartId == null) {
            cartId = UUID.randomUUID().toString();
            CookieUtils.setCookie(response, "CART_ID", cartId, 7 * 24 * 60 * 60);
        }
        // 장바구니에 상품 추가
        try {
            cartService.addToCart(dto, cartId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "장바구니에 추가되었습니다.");
            result.put("cartId", cartId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error while adding to cart " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "장바구니 추가에 실패했습니다: " + e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }

    }
}
