package com.ootd.ootd.controller.user;

import com.ootd.ootd.model.dto.user.SignupRequest;
import com.ootd.ootd.model.dto.user.UserDTO;
import com.ootd.ootd.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        // UI에 필요한 데이터를 추가적으로 전달 가능
        model.addAttribute("title", "회원가입");
        return "user/signup"; // templates/view/signup.html로 매핑
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            // 사용자 이름 중복 확인
            if (userService.existsByUsername(signupRequest.getUsername())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Error: 이미 사용 중인 사용자 이름입니다!");
                return ResponseEntity.badRequest().body(response);
            }

            // 이메일 중복 확인
            if (userService.existsByEmail(signupRequest.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Error: 이미 사용 중인 이메일입니다!");
                return ResponseEntity.badRequest().body(response);
            }

            // 사용자 등록
            UserDTO userDTO = userService.registerUser(signupRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "사용자가 성공적으로 등록되었습니다!");
            response.put("user", userDTO);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error: 회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
//        @GetMapping
//        public String showMypage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
//            String email = userDetails.getUsername(); // JWT 기반이라면 email 또는 userId 반환
//            model.addAttribute("user", userService.findByEmail(email));
//            return "mypage";
//        }
//    }

}