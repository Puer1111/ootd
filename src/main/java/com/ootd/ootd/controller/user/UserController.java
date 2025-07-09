package com.ootd.ootd.controller.user;

import com.ootd.ootd.model.dto.user.SignupRequest;
import com.ootd.ootd.model.dto.user.UserDTO;
import com.ootd.ootd.service.user.UserService;
import com.ootd.ootd.service.reward.RewardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    public UserController(UserService userService, RewardService rewardService) {
        this.userService = userService;
        this.rewardService = rewardService;
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

            // 🎉 회원가입 축하 적립금 지급
            try {
                rewardService.giveSignupReward(userDTO.getId());
                System.out.println("🎉 회원가입 축하 적립금 지급 완료 - 사용자ID: " + userDTO.getId());
            } catch (Exception e) {
                System.err.println("❌ 회원가입 적립금 지급 실패: " + e.getMessage());
                // 적립금 지급 실패해도 회원가입은 성공으로 처리
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
}