package com.ootd.ootd.controller.user;

import com.ootd.ootd.model.dto.user.SignupRequest;
import com.ootd.ootd.model.dto.user.UserDTO;
import com.ootd.ootd.service.auth.user.impl.UserDetailsImpl;
import com.ootd.ootd.service.payment.PaymentService;
import com.ootd.ootd.service.user.UserService;
import com.ootd.ootd.service.reward.RewardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final RewardService rewardService;
    private final PaymentService paymentService;

    @Autowired
    public UserController(UserService userService, RewardService rewardService, PaymentService paymentService) {
        this.userService = userService;
        this.rewardService = rewardService;
        this.paymentService = paymentService;
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("title", "회원가입");
        return "view/user/signup";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            if (userService.existsByUsername(signupRequest.getUsername())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Error: 이미 사용 중인 사용자 이름입니다!");
                return ResponseEntity.badRequest().body(response);
            }

            if (userService.existsByEmail(signupRequest.getEmail())) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Error: 이미 사용 중인 이메일입니다!");
                return ResponseEntity.badRequest().body(response);
            }

            UserDTO userDTO = userService.registerUser(signupRequest);

            try {
                rewardService.giveSignupReward(userDTO.getId());
                System.out.println("🎉 회원가입 축하 적립금 지급 완료 - 사용자ID: " + userDTO.getId());
            } catch (Exception e) {
                System.err.println("❌ 회원가입 적립금 지급 실패: " + e.getMessage());
            }

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
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            // 401 Unauthorized 에러를 반환하여 클라이언트가 로그인 필요 상태임을 명확히 알림
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userDetails.getUser().getId());
        userInfo.put("email", userDetails.getUser().getEmail());
        userInfo.put("username", userDetails.getUser().getUsername());

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/check-Pay")
    public ResponseEntity<?> checkUserPay(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        boolean result = paymentService.checkUserPay(userId);
        return ResponseEntity.ok(result);
    }
}