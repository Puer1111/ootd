package com.ootd.ootd.controller.user;

import com.ootd.ootd.model.dto.user.SignupRequest;
import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private ProductService productService;

    @GetMapping("/signup")
    public String signupForm() {
        return "view/user/signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupRequest request) {
        return "redirect:view/login";
    }

    @GetMapping("/main")
    public String mainPage(Model model) {
        try {
            System.out.println("=== 메인 페이지 로딩 시작 ===");

            List<ProductDTO> products = productService.getAllProducts();
            System.out.println("가져온 상품 개수: " + (products != null ? products.size() : 0));

            model.addAttribute("products", products);

            if (products != null && !products.isEmpty()) {
                System.out.println("첫 번째 상품: " + products.get(0).getProductName());
            } else {
                System.out.println("⚠️ 상품 목록이 비어있습니다!");
            }

            return "view/index";
        } catch (Exception e) {
            System.err.println("❌ 메인 페이지 로딩 실패: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/recommended";
        }
    }
}