package com.ootd.ootd.controller.user;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.category.Category;
import com.ootd.ootd.model.entity.order.Order;
import com.ootd.ootd.model.entity.user.User;
import com.ootd.ootd.repository.category.CategoryRepository;
import com.ootd.ootd.repository.order.OrderRepository;
import com.ootd.ootd.repository.product.ProductLikeRepository;
import com.ootd.ootd.repository.product.ProductReviewRepository;
import com.ootd.ootd.repository.user.UserRepository;
import com.ootd.ootd.security.JwtTokenProvider;
import com.ootd.ootd.service.payment.PaymentService;
import com.ootd.ootd.service.product.ProductService;
import com.ootd.ootd.service.reward.RewardService;
import com.ootd.ootd.service.user.UserService;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "로그인");
        return "view/user/login";
    }

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

    @GetMapping("/order-history")
    public String orderHistory() {
        return "view/user/orderHistory";
    }


    @GetMapping("/api/auth/order-history")
    @ResponseBody
    public ResponseEntity<?> getUserOrderHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📋 주문 내역 조회 시작 - 사용자: " + userDetails.getUsername());

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<Order> orders = orderRepository.findByUserIdAndOrderStatusOrderByOrderDateDesc(
                    user.getId(), "success"
            );

            System.out.println("📋 조회된 주문 개수: " + orders.size());

            List<Map<String, Object>> orderedProducts = orders.stream()
                    .map(order -> {
                        Map<String, Object> productMap = new HashMap<>();

                        productMap.put("productName", order.getProductName());
                        productMap.put("price", order.getProductPrice());
                        productMap.put("quantity", order.getQuantity());
                        productMap.put("totalPrice", order.getTotalPrice());
                        productMap.put("orderDate", order.getOrderDate().toString());
                        productMap.put("orderStatus", order.getOrderStatus());
                        productMap.put("orderId", order.getOrderId());

                        List<String> imageUrls = new ArrayList<>();
                        try {
                            List<ProductDTO> allProducts = productService.getAllProducts();
                            ProductDTO matchedProduct = allProducts.stream()
                                    .filter(p -> p.getProductName().equals(order.getProductName()))
                                    .findFirst()
                                    .orElse(null);

                            if (matchedProduct != null && matchedProduct.getImageUrls() != null) {
                                imageUrls = matchedProduct.getImageUrls();
                            }
                        } catch (Exception e) {
                        }

                        productMap.put("imageUrls", imageUrls);
                        productMap.put("brandName", "OOTD");
                        productMap.put("categoryName", "패션");
                        productMap.put("subCategory", "일반");

                        return productMap;
                    })
                    .collect(Collectors.toList());

            System.out.println("📋 주문 내역 처리 완료");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalCount", orders.size(),
                    "orderedProducts", orderedProducts
            ));

        } catch (Exception e) {
            System.err.println("❌ 주문 내역 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "주문 내역을 불러올 수 없습니다."
            ));
        }
    }

    @GetMapping("/cancel-history")
    public String cancelHistory() {
        return "view/user/cancelHistory";
    }

    @GetMapping("/api/auth/cancel-history")
    @ResponseBody
    public ResponseEntity<?> getUserCancelHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("📋 취소 내역 조회 시작 - 사용자: " + userDetails.getUsername());

            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "로그인이 필요합니다"
                ));
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<Order> cancelledOrders = orderRepository.findByUserIdAndOrderStatusOrderByOrderDateDesc(
                    user.getId(), "cancelled"
            );

            System.out.println("📋 조회된 취소 개수: " + cancelledOrders.size());

            List<Map<String, Object>> cancelledProducts = cancelledOrders.stream()
                    .map(order -> {
                        Map<String, Object> productMap = new HashMap<>();

                        productMap.put("productName", order.getProductName());
                        productMap.put("price", order.getProductPrice());
                        productMap.put("quantity", order.getQuantity());
                        productMap.put("totalPrice", order.getTotalPrice());
                        productMap.put("orderDate", order.getOrderDate().toString());
                        productMap.put("orderStatus", order.getOrderStatus());
                        productMap.put("orderId", order.getOrderId());

                        List<String> imageUrls = new ArrayList<>();
                        try {
                            List<ProductDTO> allProducts = productService.getAllProducts();
                            ProductDTO matchedProduct = allProducts.stream()
                                    .filter(p -> p.getProductName().equals(order.getProductName()))
                                    .findFirst()
                                    .orElse(null);

                            if (matchedProduct != null && matchedProduct.getImageUrls() != null) {
                                imageUrls = matchedProduct.getImageUrls();
                            }
                        } catch (Exception e) {
                            // 에러 시 빈 배열 유지
                        }

                        productMap.put("imageUrls", imageUrls);
                        productMap.put("brandName", "OOTD");
                        productMap.put("categoryName", "패션");
                        productMap.put("subCategory", "일반");

                        return productMap;
                    })
                    .collect(Collectors.toList());

            System.out.println("📋 취소 내역 처리 완료");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalCount", cancelledOrders.size(),
                    "cancelledProducts", cancelledProducts
            ));

        } catch (Exception e) {
            System.err.println("❌ 취소 내역 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "취소 내역을 가져오는 중 오류가 발생했습니다."
            ));
        }
    }

@PostMapping("/api/auth/cancel-order/{orderId}")
@ResponseBody
public ResponseEntity<?> cancelOrderById(@PathVariable Long orderId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
    try {
        System.out.println("📋 주문 취소 시작 - OrderID: " + orderId);

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다"
            ));
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        Optional<Order> orderOpt = orderRepository.findByOrderIdAndUserId(orderId, user.getId());

        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "주문을 찾을 수 없습니다"
            ));
        }

        Order order = orderOpt.get();

        if (!"success".equals(order.getOrderStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "취소할 수 없는 주문입니다"
            ));
        }

        try {
            String impUid = paymentService.getImpUid(orderId);
            if (impUid != null && !impUid.isEmpty()) {
                IamportResponse<Payment> cancelResult = paymentService.cancelPayment(impUid);
                if (cancelResult != null) {
                    System.out.println("💰 결제 취소 완료 - OrderID: " + orderId + ", impUid: " + impUid);
                } else {
                    System.err.println("❌ 결제 취소 실패 - OrderID: " + orderId);
                }
            } else {
                System.out.println("⚠️ impUid가 없어서 결제 취소를 건너뜁니다.");
            }
        } catch (Exception e) {
            System.err.println("❌ 결제 취소 중 오류: " + e.getMessage());
        }

        order.setOrderStatus("cancelled");
        orderRepository.save(order);

        try {
            List<UserOrder> userOrders = userOrderRepository.findByUserId(user.getId());

            for (UserOrder userOrder : userOrders) {
                if (userOrder.getStatus() == UserOrder.OrderStatus.ORDERED) {
                    userOrder.cancel();
                    userOrderRepository.save(userOrder);
                    System.out.println("📋 UserOrder 취소 완료 - ID: " + userOrder.getId());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ UserOrder 취소 실패: " + e.getMessage());
        }

        System.out.println("📋 주문 취소 완료 - OrderID: " + orderId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "주문이 취소되었습니다"
        ));

    } catch (Exception e) {
        System.err.println("❌ 주문 취소 실패: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "주문 취소 중 오류가 발생했습니다: " + e.getMessage()
        ));
    }
}

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

            Long availablePoints = 0L;
            try {
                availablePoints = rewardService.getAvailablePoints(user.getId());
                System.out.println("✅ 적립금 조회 성공 - 사용자ID: " + user.getId() + ", 적립금: " + availablePoints + "원");
            } catch (Exception e) {
                System.err.println("❌ 적립금 조회 실패: " + e.getMessage());
            }

            int reviewCount = 0;
            try {
                if (productReviewRepository != null) {
                    reviewCount = productReviewRepository.countByUserId(user.getId());
                    System.out.println("✅ 리뷰 개수 조회 성공 - 사용자ID: " + user.getId() + ", 리뷰 개수: " + reviewCount + "개");
                }
            } catch (Exception e) {
                System.err.println("❌ 리뷰 개수 조회 실패: " + e.getMessage());
            }

            int coupons = 0;

            response.put("success", true);
            response.put("points", availablePoints);
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