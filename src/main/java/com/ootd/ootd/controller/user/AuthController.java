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
        return "view/user/signup";  // signup.html이 templates/user 아래 있어야 함
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupRequest request) {
        // 회원가입 처리 로직
        return "redirect:view/login";  // 회원가입 후 로그인으로
    }

    @GetMapping("/main")
    public String mainPage(Model model) {
        try {
            System.out.println("=== 메인 페이지 로딩 시작 ===");

            // 전체 상품 목록을 가져와서 메인 페이지에 표시
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
            // 에러 발생 시 추천 페이지로 리다이렉트
            return "redirect:/recommended";
        }
    }
}