package com.ootd.ootd.controller.user;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.category.Category;
import com.ootd.ootd.model.entity.user.User;
import com.ootd.ootd.repository.category.CategoryRepository;
import com.ootd.ootd.repository.product.ProductLikeRepository;
import com.ootd.ootd.repository.product.ProductReviewRepository;
import com.ootd.ootd.repository.user.UserRepository;
import com.ootd.ootd.security.JwtTokenProvider;
import com.ootd.ootd.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ootd.ootd.repository.order.UserOrderRepository;
import com.ootd.ootd.model.entity.order.UserOrder;
import com.ootd.ootd.repository.order.UserOrderRepository;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserOrderRepository userOrderRepository;

    @Autowired
    private CategoryRepository categoryRepository;



    // 로그인 페이지 보여주기
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "로그인");
        return "view/user/login";
    }

    // 로그인 처리 API
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (passwordEncoder.matches(password, user.getPassword())) {
                    String token = jwtTokenProvider.createToken(user.getEmail(), user.getRoles());

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
                    response.put("success", false);
                    response.put("message", "비밀번호가 일치하지 않습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "등록되지 않은 이메일입니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/mypage")
    public String goPage(Model model) {
        System.out.println("마이페이지 접근 - 페이지 로드");
        return "view/user/mypage";
    }

    @GetMapping("/liked-products")
    public String likedProducts() {
        return "view/product/likedProducts";
    }

    @GetMapping("/api/auth/mypage")
    @ResponseBody
    public ResponseEntity<?> myPage(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "인증되지 않은 사용자입니다."));
            }

            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            Map<String, String> userInfo = new HashMap<>();
            if (user != null) {
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("name", user.getName());
                userInfo.put("phone", user.getPhone());
            } else {
                userInfo.put("username", userDetails.getUsername());
                userInfo.put("email", userDetails.getUsername());
                userInfo.put("name", "사용자");
                userInfo.put("phone", "정보 없음");
            }

            response.put("user", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/api/auth/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody Map<String, String> passwordRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String currentPassword = passwordRequest.get("currentPassword");
            String newPassword = passwordRequest.get("newPassword");
            String confirmPassword = passwordRequest.get("confirmPassword");

            if (currentPassword == null || newPassword == null || confirmPassword == null) {
                response.put("success", false);
                response.put("message", "모든 필드를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "새 비밀번호는 최소 6자 이상이어야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

            if (user == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                response.put("success", false);
                response.put("message", "현재 비밀번호가 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/api/auth/liked-products")
    @ResponseBody
    public ResponseEntity<?> getUserLikedProducts(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<Long> likedProductNos = productLikeRepository.findProductNosByUserId(user.getId());

            List<ProductDTO> likedProducts = new ArrayList<>();
            for (Long productNo : likedProductNos) {
                ProductDTO product = productService.getProductById(productNo);
                if (product != null) {
                    product.setLikeCount(productLikeRepository.countByProductNo(productNo));
                    if (productReviewRepository != null) {
                        product.setReviewCount(productReviewRepository.countByProductNo(productNo));
                        Double avgRating = productReviewRepository.findAverageRatingByProductNo(productNo);
                        product.setAverageRating(avgRating != null ? avgRating : 0.0);
                    }
                    likedProducts.add(product);
                }
            }

            response.put("success", true);
            response.put("likedProducts", likedProducts);
            response.put("totalCount", likedProducts.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "좋아요 상품 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 주문 내역 페이지
    @GetMapping("/order-history")
    public String orderHistory() {
        return "view/user/orderHistory";
    }

    // 주문 내역 API (좋아요 목록과 완전히 동일)
    // LoginController.java의 주문내역 API (디버깅 제거된 버전)

    @GetMapping("/api/auth/order-history")
    @ResponseBody
    public ResponseEntity<?> getUserOrderHistory(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<UserOrder> userOrders;
            try {
                userOrders = userOrderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                        user.getId(), UserOrder.OrderStatus.ORDERED);
            } catch (Exception e) {
                userOrders = userOrderRepository.findAll().stream()
                        .filter(order -> order.getUserId().equals(user.getId()) &&
                                order.getStatus() == UserOrder.OrderStatus.ORDERED)
                        .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                        .collect(Collectors.toList());
            }

            List<Map<String, Object>> orderedProducts = new ArrayList<>();

            for (UserOrder order : userOrders) {
                ProductDTO product = productService.getProductById(order.getProductNo());
                if (product != null) {
                    // 🔥 카테고리 정보 조회 추가
                    String categoryName = "카테고리";
                    String subCategoryName = "하위카테고리";

                    if (product.getCategoryNo() != null) {
                        Optional<Category> categoryOpt = categoryRepository.findById(product.getCategoryNo());
                        if (categoryOpt.isPresent()) {
                            Category category = categoryOpt.get();
                            categoryName = category.getMainCategory();
                            subCategoryName = category.getSubCategory();
                        }
                    }

                    Map<String, Object> orderWithProduct = new HashMap<>();

                    // 상품 정보
                    orderWithProduct.put("productNo", product.getProductNo());
                    orderWithProduct.put("productName", product.getProductName());
                    orderWithProduct.put("price", product.getPrice());
                    orderWithProduct.put("imageUrls", product.getImageUrls());
                    orderWithProduct.put("brandName", product.getBrandName());

                    // 🔥 카테고리 정보 추가
                    orderWithProduct.put("categoryName", categoryName);
                    orderWithProduct.put("subCategory", subCategoryName);

                    // 주문 정보 추가
                    orderWithProduct.put("orderId", order.getId());
                    orderWithProduct.put("quantity", order.getQuantity() != null ? order.getQuantity() : 1);
                    orderWithProduct.put("totalPrice", order.getTotalPrice() != null ? order.getTotalPrice() : 0L);
                    orderWithProduct.put("orderDate", order.getCreatedAt());
                    orderWithProduct.put("orderStatus", order.getStatus().getDescription());

                    orderedProducts.add(orderWithProduct);
                }
            }

            response.put("success", true);
            response.put("orderedProducts", orderedProducts);
            response.put("totalCount", orderedProducts.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "주문 내역을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 취소 내역 페이지
    @GetMapping("/cancel-history")
    public String cancelHistory() {
        return "view/user/cancelHistory";
    }

    // 취소 내역 API
    @GetMapping("/api/auth/cancel-history")
    @ResponseBody
    public ResponseEntity<?> getUserCancelHistory(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // 취소한 상품 번호 목록
            List<Long> cancelledProductNos = userOrderRepository.findCancelledProductNosByUserId(user.getId());

            List<ProductDTO> cancelledProducts = new ArrayList<>();
            for (Long productNo : cancelledProductNos) {
                ProductDTO product = productService.getProductById(productNo);
                if (product != null) {
                    product.setLikeCount(productLikeRepository.countByProductNo(productNo));
                    if (productReviewRepository != null) {
                        product.setReviewCount(productReviewRepository.countByProductNo(productNo));
                        Double avgRating = productReviewRepository.findAverageRatingByProductNo(productNo);
                        product.setAverageRating(avgRating != null ? avgRating : 0.0);
                    }
                    cancelledProducts.add(product);
                }
            }

            response.put("success", true);
            response.put("cancelledProducts", cancelledProducts);
            response.put("totalCount", cancelledProducts.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "취소 내역을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 주문 취소 API (주문내역에서)
    @PostMapping("/api/auth/cancel-order/{orderId}")
    @ResponseBody
    public ResponseEntity<?> cancelOrderById(@PathVariable Long orderId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            Optional<UserOrder> orderOpt = userOrderRepository.findByIdAndUserId(orderId, user.getId());

            if (orderOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "주문을 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }

            UserOrder order = orderOpt.get();
            if (order.getStatus() != UserOrder.OrderStatus.ORDERED) {
                response.put("success", false);
                response.put("message", "이미 처리된 주문입니다");
                return ResponseEntity.badRequest().body(response);
            }

            order.cancel();
            userOrderRepository.save(order);

            response.put("success", true);
            response.put("message", "주문이 취소되었습니다");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "주문 취소 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // LoginController.java에 추가할 사용자 통계 API

    // LoginController.java의 사용자 통계 API (수정된 버전)

    @GetMapping("/api/auth/user-stats")
    @ResponseBody
    public ResponseEntity<?> getUserStats(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // 🔧 후기(리뷰) 개수 가져오기 - countByUserId 메소드 사용
            int reviewCount = 0;
            if (productReviewRepository != null) {
                reviewCount = productReviewRepository.countByUserId(user.getId());
            }

            // 적립금과 쿠폰은 나중에 구현 (기본값 0)
            int points = 0; // TODO: 적립금 시스템 구현 시 실제 값으로 변경
            int coupons = 0; // TODO: 쿠폰 시스템 구현 시 실제 값으로 변경

            response.put("success", true);
            response.put("reviewCount", reviewCount); // 내가 쓴 후기(리뷰) 개수
            response.put("points", points);
            response.put("coupons", coupons);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "통계 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}