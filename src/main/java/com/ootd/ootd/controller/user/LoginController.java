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
import com.ootd.ootd.service.reward.RewardService;
import com.ootd.ootd.service.user.UserService;
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

    // 🆕 RewardService 사용 (pointService 대신)
    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserService userService;

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
            System.out.println("=== 좋아요 상품 API 시작 ===");

            if (userDetails == null) {
                System.out.println("❌ userDetails가 null입니다");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            System.out.println("✅ 사용자 인증 확인: " + userDetails.getUsername());

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            System.out.println("✅ 사용자 조회 성공: ID=" + user.getId());

            List<Long> likedProductNos = productLikeRepository.findProductNosByUserId(user.getId());
            System.out.println("✅ 좋아요 상품 번호 조회 성공: " + likedProductNos.size() + "개");

            List<ProductDTO> likedProducts = new ArrayList<>();

            for (Long productNo : likedProductNos) {
                try {
                    System.out.println("상품 조회 시작: productNo=" + productNo);

                    ProductDTO product = productService.getProductById(productNo);
                    if (product != null) {
                        System.out.println("✅ 상품 조회 성공: " + product.getProductName());

                        // null 값 강제 설정으로 안전성 확보
                        if (product.getIsActiveSale() == null) {
                            product.setIsActiveSale(false);
                        }
                        if (product.getIsRecommended() == null) {
                            product.setIsRecommended(false);
                        }
                        if (product.getIsSale() == null) {
                            product.setIsSale(false);
                        }

                        product.setLikeCount(productLikeRepository.countByProductNo(productNo));

                        if (productReviewRepository != null) {
                            product.setReviewCount(productReviewRepository.countByProductNo(productNo));
                            Double avgRating = productReviewRepository.findAverageRatingByProductNo(productNo);
                            product.setAverageRating(avgRating != null ? avgRating : 0.0);
                        }

                        likedProducts.add(product);
                        System.out.println("✅ 상품 처리 완료: " + product.getProductName());
                    } else {
                        System.out.println("❌ 상품 조회 실패: productNo=" + productNo);
                    }
                } catch (Exception e) {
                    System.err.println("❌ 상품 처리 중 에러 - productNo: " + productNo);
                    e.printStackTrace();
                }
            }

            System.out.println("✅ 모든 상품 처리 완료: " + likedProducts.size() + "개");

            response.put("success", true);
            response.put("likedProducts", likedProducts);
            response.put("totalCount", likedProducts.size());

            System.out.println("✅ 응답 객체 생성 완료");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 좋아요 상품 API 전체 실패: " + e.getMessage());
            e.printStackTrace();

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

    // 기존 getUserOrderHistory 메서드를 다음과 같이 수정
    @GetMapping("/api/auth/order-history")
    @ResponseBody
    public ResponseEntity<?> getUserOrderHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📋 주문 내역 조회 요청 - 사용자: " + userDetails.getUsername());

            // 사용자 정보 조회
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // UserOrder 조회
            List<UserOrder> userOrders = userOrderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                    user.getId(), UserOrder.OrderStatus.ORDERED
            );

            System.out.println("📋 조회된 주문 개수: " + userOrders.size());

            List<Map<String, Object>> orderedProducts = userOrders.stream()
                    .map(order -> {
                        Map<String, Object> productMap = new HashMap<>();
                        productMap.put("productNo", order.getProductNo());

                        // 상품 정보 조회
                        ProductDTO productDTO = null;
                        try {
                            productDTO = productService.getProductById(order.getProductNo());
                        } catch(Exception e) {
                            System.err.println("❌ 상품 정보 조회 실패: " + order.getProductNo());
                        }

                        if (productDTO != null) {
                            productMap.put("productName", productDTO.getProductName());
                            productMap.put("imageUrls", productDTO.getImageUrls() != null ? productDTO.getImageUrls() : new ArrayList<>());
                            productMap.put("brandName", productDTO.getBrandName());
                            productMap.put("categoryName", productDTO.getMainCategory() != null ? productDTO.getMainCategory() : "");
                            productMap.put("subCategory", productDTO.getSubCategory() != null ? productDTO.getSubCategory() : "");
                        } else {
                            productMap.put("productName", "상품정보없음");
                            productMap.put("imageUrls", new ArrayList<>());
                            productMap.put("brandName", "");
                            productMap.put("categoryName", "");
                            productMap.put("subCategory", "");
                        }

                        productMap.put("price", order.getTotalPrice() / order.getQuantity());
                        productMap.put("quantity", order.getQuantity());
                        productMap.put("totalPrice", order.getTotalPrice());
                        productMap.put("orderDate", order.getCreatedAt().toString());
                        productMap.put("orderStatus", order.getStatus().getDescription());
                        productMap.put("orderId", order.getId());

                        return productMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalCount", userOrders.size(),
                    "orderedProducts", orderedProducts
            ));
        } catch (Exception e) {
            System.err.println("❌ 주문 내역 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "주문 내역을 불러올 수 없습니다."
            ));
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
            System.out.println("=== 취소 내역 API 시작 ===");

            if (userDetails == null) {
                System.out.println("❌ userDetails가 null입니다");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            System.out.println("✅ 사용자 인증 확인: " + userDetails.getUsername());

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            System.out.println("✅ 사용자 조회 성공: ID=" + user.getId());

            List<Long> cancelledProductNos = userOrderRepository.findCancelledProductNosByUserId(user.getId());
            System.out.println("✅ 취소된 상품 번호 조회 성공: " + cancelledProductNos.size() + "개");

            List<ProductDTO> cancelledProducts = new ArrayList<>();

            for (Long productNo : cancelledProductNos) {
                try {
                    System.out.println("취소된 상품 조회 시작: productNo=" + productNo);

                    ProductDTO product = productService.getProductById(productNo);
                    if (product != null) {
                        System.out.println("✅ 취소된 상품 조회 성공: " + product.getProductName());

                        // null 값 강제 설정으로 안전성 확보
                        if (product.getIsActiveSale() == null) {
                            product.setIsActiveSale(false);
                        }
                        if (product.getIsRecommended() == null) {
                            product.setIsRecommended(false);
                        }
                        if (product.getIsSale() == null) {
                            product.setIsSale(false);
                        }

                        product.setLikeCount(productLikeRepository.countByProductNo(productNo));

                        if (productReviewRepository != null) {
                            product.setReviewCount(productReviewRepository.countByProductNo(productNo));
                            Double avgRating = productReviewRepository.findAverageRatingByProductNo(productNo);
                            product.setAverageRating(avgRating != null ? avgRating : 0.0);
                        }

                        cancelledProducts.add(product);
                        System.out.println("✅ 취소된 상품 처리 완료: " + product.getProductName());
                    } else {
                        System.out.println("❌ 취소된 상품 조회 실패: productNo=" + productNo);
                    }
                } catch (Exception e) {
                    System.err.println("❌ 취소된 상품 처리 중 에러 - productNo: " + productNo);
                    e.printStackTrace();
                }
            }

            System.out.println("✅ 모든 취소된 상품 처리 완료: " + cancelledProducts.size() + "개");

            response.put("success", true);
            response.put("cancelledProducts", cancelledProducts);
            response.put("totalCount", cancelledProducts.size());

            System.out.println("✅ 취소 내역 응답 객체 생성 완료");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 취소 내역 API 전체 실패: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "취소 내역을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 주문 취소 API (주문내역에서)
//    @PostMapping("/api/auth/cancel-order/{orderId}")
//    @ResponseBody
//    public ResponseEntity<?> cancelOrderById(@PathVariable Long orderId,
//                                             @AuthenticationPrincipal UserDetails userDetails) {
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            if (userDetails == null) {
//                response.put("success", false);
//                response.put("message", "로그인이 필요합니다");
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//            }
//
//            User user = userRepository.findByEmail(userDetails.getUsername())
//                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
//
//            Optional<UserOrder> orderOpt = userOrderRepository.findByIdAndUserId(orderId, user.getId());
//
//            if (orderOpt.isEmpty()) {
//                response.put("success", false);
//                response.put("message", "주문을 찾을 수 없습니다");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            UserOrder order = orderOpt.get();
//            if (order.getStatus() != UserOrder.OrderStatus.ORDERED) {
//                response.put("success", false);
//                response.put("message", "이미 처리된 주문입니다");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            order.cancel();
//            userOrderRepository.save(order);
//
//            response.put("success", true);
//            response.put("message", "주문이 취소되었습니다");
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("message", "주문 취소 중 오류가 발생했습니다: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }

    // 🆕 사용자 통계 API (RewardService 사용)
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

            // 🆕 실제 적립금 조회 (RewardService 사용)
            Long availablePoints = 0L;
            try {
                availablePoints = rewardService.getAvailablePoints(user.getId());
                System.out.println("✅ 적립금 조회 성공 - 사용자ID: " + user.getId() + ", 적립금: " + availablePoints + "원");
            } catch (Exception e) {
                System.err.println("❌ 적립금 조회 실패: " + e.getMessage());
                // 기본값 0 사용
            }

            // 후기(리뷰) 개수 가져오기
            int reviewCount = 0;
            try {
                if (productReviewRepository != null) {
                    reviewCount = productReviewRepository.countByUserId(user.getId());
                    System.out.println("✅ 리뷰 개수 조회 성공 - 사용자ID: " + user.getId() + ", 리뷰 개수: " + reviewCount + "개");
                }
            } catch (Exception e) {
                System.err.println("❌ 리뷰 개수 조회 실패: " + e.getMessage());
            }

            // 쿠폰은 나중에 구현 (기본값 0)
            int coupons = 0;

            response.put("success", true);
            response.put("points", availablePoints); // 🆕 실제 적립금
            response.put("reviewCount", reviewCount);
            response.put("coupons", coupons);

            System.out.println("📊 최종 통계 - 적립금: " + availablePoints + "원, 리뷰: " + reviewCount + "개, 쿠폰: " + coupons + "장");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 사용자 통계 조회 전체 실패: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "통계 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}