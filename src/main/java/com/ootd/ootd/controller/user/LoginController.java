package com.ootd.ootd.controller.user;

import com.ootd.ootd.model.entity.User; // User 엔티티 임포트
import com.ootd.ootd.repository.UserRepository; // UserRepository 임포트
import com.ootd.ootd.security.JwtTokenProvider; // JwtTokenProvider 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder 임포트
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional; // Optional 임포트


@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 로그인 페이지 보여주기
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "로그인");
        return "user/login";
    }

    // 로그인 처리 API
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) { // 모델로 바꾸면 415
        Map<String, Object> response = new HashMap<>();

        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            // 이메일로 사용자 찾기
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // 비밀번호 검증
                if (passwordEncoder.matches(password, user.getPassword())) {
                    // 로그인 성공, JWT 토큰 생성
                    String token = jwtTokenProvider.createToken(user.getUsername(), user.getRoles());

                    response.put("success", true);
                    response.put("message", "로그인 성공");
                    response.put("token", token);
                    response.put("user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "name", user.getName()
                    ));

                    return ResponseEntity.ok(response);
                } else {
                    // 비밀번호 불일치
                    response.put("success", false);
                    response.put("message", "비밀번호가 일치하지 않습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                // 사용자 없음
                response.put("success", false);
                response.put("message", "등록되지 않은 이메일입니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace(); // 실제 오류 출력 추가
            response.put("success", false);
            response.put("message", "로그인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }

    }

    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login?error=authentication_required";
        }

        try {
            Optional<User> userOptional = userRepository.findByEmail(userDetails.getUsername());

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                model.addAttribute("user", user);
                return "user/mypage";
            } else {
                return "redirect:/login?error=user_not_found";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login?error=server_error";
        }
    }
}