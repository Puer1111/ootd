package com.ootd.ootd.controller.user;

import com.ootd.ootd.model.dto.user.SignupRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @GetMapping("/signup")
    public String signupForm() {
        return "user/signup";  // signup.html이 templates/user 아래 있어야 함
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupRequest request) {
        // 회원가입 처리 로직
        return "redirect:/login";  // 회원가입 후 로그인으로
    }

    @GetMapping("/main")
    public String mainPage() {
        return "user/main";
    }
}

