package com.ootd.ootd.controller.product;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.like.ProductLike;
import com.ootd.ootd.model.entity.product_colors.ProductColors;
import com.ootd.ootd.model.entity.review.ProductReview;
import com.ootd.ootd.model.entity.user.User;
import com.ootd.ootd.repository.product.ProductLikeRepository;
import com.ootd.ootd.repository.product.ProductRepository;
import com.ootd.ootd.repository.product.ProductReviewRepository;
import com.ootd.ootd.repository.user.UserRepository;
import com.ootd.ootd.service.colors.ColorsService;
import com.ootd.ootd.service.product.ProductService;
import com.ootd.ootd.utils.service.GoogleCloudStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import com.ootd.ootd.model.entity.order.UserOrder;
import com.ootd.ootd.repository.order.UserOrderRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProductController {


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(1024);
    }

    @Autowired
    ProductService productService;
    @Autowired
    ColorsService colorsService;
    @Autowired
    GoogleCloudStorageService googleCloudStorageService;
    @Autowired
    ProductReviewRepository productReviewRepository;
    @Autowired
    ProductLikeRepository productLikeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserOrderRepository userOrderRepository;


    public ProductController(ProductService productService, GoogleCloudStorageService googleCloudStorageService, ColorsService colorsService) {
        this.productService = productService;
        this.googleCloudStorageService = googleCloudStorageService;
        this.colorsService = colorsService;
    }

    // ProductController.java에 추가
    @GetMapping("/")
    public String test(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "view/index";
    }

    @GetMapping("/products/{productNo}")
    public String productDetail(@PathVariable Long productNo, Model model) {
        ProductDTO product = productService.getProductById(productNo);
        model.addAttribute("product", product);
        return "view/product/productDetail";
    }

    @GetMapping("/enter")
    public String showEnterProductForm(){
        return "view/product/enterProduct";
    }

    @PostMapping("/enter/product")
    public ResponseEntity<?> insertProduct(@ModelAttribute ProductDTO dto,
                                           HttpServletRequest request
    )  {
//        String[] rawColors = request.getParameterValues("colorsNo");
//        System.out.println("rawColors: " + Arrays.toString(rawColors));
        ProductDTO productDTO;

        try {
//            if (rawColors != null && rawColors.length > 0) {
//                List<Long> colorsNoList = Arrays.stream(rawColors)
//                        .map(Long::parseLong)
//                        .collect(Collectors.toList());
//                dto.setColorsNo(colorsNoList);
//            }

            if (dto.getImages() != null && dto.getImages().length > 0) {
                // 배열 전체를 한 번에 전달
                List<String> images;
                images = googleCloudStorageService.uploadImages(dto.getImages());
                System.out.println("Uploaded " + dto.getImages().length + " images");
                dto.setImageUrls(images);
                System.out.println("Checked ImageUrl : " + images);
            } else {
                System.out.println("No images to upload");
            }

            // 상품색깔 테이블에 들어가는 데이터
//            ProductColors productColors = colorsService.initToProductColor(dto.getColorsNo());
//            dto.setProductColorsNo(productColors.getProductColorsNo());
            productDTO = productService.insertProduct(dto);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("product",productDTO);
        response.put("redirectUrl", "/");  // 마이페이지로 이동 엔드포인트 차후 수정.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 좋아요
    @PostMapping("/products/{productNo}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long productNo,
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

            Optional<ProductLike> existingLike = productLikeRepository
                    .findByProductNoAndUserId(productNo, user.getId());

            boolean isLiked;
            if (existingLike.isPresent()) {
                // 좋아요 취소
                productLikeRepository.delete(existingLike.get());
                isLiked = false;
            } else {
                // 좋아요 추가
                ProductLike like = new ProductLike(productNo, user.getId());
                productLikeRepository.save(like);
                isLiked = true;
            }

            int likeCount = productLikeRepository.countByProductNo(productNo);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", likeCount);
            response.put("message", isLiked ? "좋아요!" : "좋아요 취소");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 내가 한 좋아요인지 체크
    @GetMapping("/products/{productNo}/like-info")
    public ResponseEntity<?> getLikeInfo(@PathVariable Long productNo,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            int likeCount = productLikeRepository.countByProductNo(productNo);
            boolean isLiked = false;

            if (userDetails != null) {
                User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
                if (user != null) {
                    isLiked = productLikeRepository.existsByProductNoAndUserId(productNo, user.getId());
                }
            }

            response.put("success", true);
            response.put("likeCount", likeCount);
            response.put("isLiked", isLiked);
            response.put("isLoggedIn", userDetails != null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "정보를 가져오는 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 리뷰 작성
    @PostMapping("/products/{productNo}/review")
    public ResponseEntity<?> createReview(@PathVariable Long productNo,
                                          @RequestBody Map<String, Object> reviewData,
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

            // 이미 리뷰를 작성했는지 확인
            if (productReviewRepository.existsByProductNoAndUserId(productNo, user.getId())) {
                response.put("success", false);
                response.put("message", "이미 리뷰를 작성하셨습니다");
                return ResponseEntity.badRequest().body(response);
            }

            int rating = Integer.parseInt(reviewData.get("rating").toString());
            String content = reviewData.get("content").toString();

            if (rating < 1 || rating > 5) {
                response.put("success", false);
                response.put("message", "평점은 1-5점 사이여야 합니다");
                return ResponseEntity.badRequest().body(response);
            }

            ProductReview review = new ProductReview(productNo, user.getId(), rating, content);
            productReviewRepository.save(review);

            // 업데이트된 통계 정보
            int reviewCount = productReviewRepository.countByProductNo(productNo);
            Double avgRating = productReviewRepository.findAverageRatingByProductNo(productNo);

            response.put("success", true);
            response.put("message", "리뷰가 작성되었습니다");
            response.put("reviewCount", reviewCount);
            response.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "리뷰 작성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 상품의 리뷰 목록 조회
    @GetMapping("/products/{productNo}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long productNo) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ProductReview> reviews = productReviewRepository.findByProductNoOrderByCreatedAtDesc(productNo);
            int reviewCount = productReviewRepository.countByProductNo(productNo);
            Double avgRating = productReviewRepository.findAverageRatingByProductNo(productNo);

            response.put("success", true);
            response.put("reviews", reviews);
            response.put("reviewCount", reviewCount);
            response.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0.0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "리뷰를 가져오는 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 주문하기 (수량 지원 추가)
    @PostMapping("/products/{productNo}/order")
    public ResponseEntity<?> orderProduct(@PathVariable Long productNo,
                                          @RequestBody(required = false) Map<String, Object> orderRequest,
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

            // 요청에서 수량과 총 금액 가져오기 (기본값 설정)
            Integer quantity = 1;
            Long totalPrice = 0L;

            // orderRequest가 있는 경우에만 값 추출 (수량 조절 기능 사용 시)
            if (orderRequest != null) {
                if (orderRequest.containsKey("quantity")) {
                    quantity = Integer.parseInt(orderRequest.get("quantity").toString());
                }
                if (orderRequest.containsKey("totalPrice")) {
                    totalPrice = Long.parseLong(orderRequest.get("totalPrice").toString());
                }
            }

            // 유효성 검사
            if (quantity < 1 || quantity > 99) {
                response.put("success", false);
                response.put("message", "수량은 1개 이상 99개 이하여야 합니다");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미 주문했는지 확인
            boolean alreadyOrdered = userOrderRepository.existsByUserIdAndProductNoAndStatus(
                    user.getId(), productNo, UserOrder.OrderStatus.ORDERED);

            if (alreadyOrdered) {
                response.put("success", false);
                response.put("message", "이미 주문하신 상품입니다");
                return ResponseEntity.badRequest().body(response);
            }

            // 상품 정보 가져와서 총 금액 계산
            ProductDTO product = productService.getProductById(productNo);
            if (product == null) {
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }

            Long expectedTotalPrice = (long) (product.getPrice() * quantity);

            // 클라이언트에서 전송한 총 금액이 없거나 잘못된 경우 서버에서 계산
            if (totalPrice == 0L || !totalPrice.equals(expectedTotalPrice)) {
                totalPrice = expectedTotalPrice;
            }

            // 주문 추가 (수량과 총 금액 포함)
            // 기존: UserOrder order = new UserOrder(productNo, user.getId());
            UserOrder order = new UserOrder(productNo, user.getId(), quantity, totalPrice);
            userOrderRepository.save(order);

            response.put("success", true);
            response.put("message", "주문이 완료되었습니다!");
            response.put("isOrdered", true);
            response.put("orderId", order.getId());
            response.put("quantity", quantity);
            response.put("totalPrice", totalPrice);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 주문 상태 확인 API
    @GetMapping("/products/{productNo}/order-status")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long productNo,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isOrdered = false;
            Long orderId = null;

            if (userDetails != null) {
                User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
                if (user != null) {
                    Optional<UserOrder> order = userOrderRepository.findByUserIdAndProductNoAndStatus(
                            user.getId(), productNo, UserOrder.OrderStatus.ORDERED);
                    if (order.isPresent()) {
                        isOrdered = true;
                        orderId = order.get().getId();
                    }
                }
            }

            response.put("success", true);
            response.put("isOrdered", isOrdered);
            response.put("orderId", orderId);
            response.put("isLoggedIn", userDetails != null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "주문 상태를 확인하는 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 주문 취소 API
    @PostMapping("/products/{productNo}/cancel-order")
    public ResponseEntity<?> cancelOrder(@PathVariable Long productNo,
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

            Optional<UserOrder> orderOpt = userOrderRepository.findByUserIdAndProductNoAndStatus(
                    user.getId(), productNo, UserOrder.OrderStatus.ORDERED);

            if (orderOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "주문을 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }

            UserOrder order = orderOpt.get();
            order.cancel();
            userOrderRepository.save(order);

            response.put("success", true);
            response.put("message", "주문이 취소되었습니다");
            response.put("isOrdered", false);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "주문 취소 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 🆕 구매 후기 작성 (주문한 사용자만 가능)
    @PostMapping("/products/{productNo}/after-review")
    public ResponseEntity<?> createAfterReview(@PathVariable Long productNo,
                                               @RequestBody Map<String, Object> reviewData,
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

            // 🔥 주문 여부 확인 (구매 후기는 실제 구매한 사람만 작성 가능)
            boolean hasOrdered = userOrderRepository.existsByUserIdAndProductNoAndStatus(
                    user.getId(), productNo, UserOrder.OrderStatus.ORDERED);

            if (!hasOrdered) {
                response.put("success", false);
                response.put("message", "구매 후기는 실제 구매하신 고객만 작성 가능합니다");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미 구매 후기를 작성했는지 확인
            if (productReviewRepository.existsByProductNoAndUserId(productNo, user.getId())) {
                response.put("success", false);
                response.put("message", "이미 구매 후기를 작성하셨습니다");
                return ResponseEntity.badRequest().body(response);
            }

            int rating = Integer.parseInt(reviewData.get("rating").toString());
            String content = reviewData.get("content").toString();

            if (rating < 1 || rating > 5) {
                response.put("success", false);
                response.put("message", "평점은 1-5점 사이여야 합니다");
                return ResponseEntity.badRequest().body(response);
            }

            // 🔥 구매 후기는 별도 필드로 구분 (예: type 필드 추가 고려)
            ProductReview afterReview = new ProductReview(productNo, user.getId(), rating, content);
            // afterReview.setReviewType("AFTER_REVIEW"); // 필요시 타입 구분
            productReviewRepository.save(afterReview);

            response.put("success", true);
            response.put("message", "구매 후기가 작성되었습니다");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "구매 후기 작성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 🆕 구매 후기 목록 조회 (주문한 사용자의 후기만)
    @GetMapping("/products/{productNo}/after-reviews")
    public ResponseEntity<?> getAfterReviews(@PathVariable Long productNo) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 🔥 실제 구매한 사용자들의 리뷰만 조회하는 로직 필요
            // 현재는 모든 리뷰를 구매 후기로 간주
            List<ProductReview> afterReviews = productReviewRepository.findByProductNoOrderByCreatedAtDesc(productNo);

            // 🔥 실제로는 주문 여부를 확인하여 필터링해야 함
            // afterReviews = afterReviews.stream()
            //     .filter(review -> userOrderRepository.existsByUserIdAndProductNoAndStatus(
            //         review.getUserId(), productNo, UserOrder.OrderStatus.ORDERED))
            //     .collect(Collectors.toList());

            response.put("success", true);
            response.put("afterReviews", afterReviews);
            response.put("afterReviewCount", afterReviews.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "구매 후기를 가져오는 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 🆕 구매 후기 작성 권한 확인
    @GetMapping("/products/{productNo}/after-review-permission")
    public ResponseEntity<?> getAfterReviewPermission(@PathVariable Long productNo,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean canWriteAfterReview = false;
            boolean isLoggedIn = userDetails != null;

            if (isLoggedIn) {
                User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
                if (user != null) {
                    // 주문했는지 확인
                    boolean hasOrdered = userOrderRepository.existsByUserIdAndProductNoAndStatus(
                            user.getId(), productNo, UserOrder.OrderStatus.ORDERED);

                    // 이미 후기를 작성했는지 확인
                    boolean alreadyReviewed = productReviewRepository.existsByProductNoAndUserId(productNo, user.getId());

                    canWriteAfterReview = hasOrdered && !alreadyReviewed;
                }
            }

            response.put("success", true);
            response.put("canWriteAfterReview", canWriteAfterReview);
            response.put("isLoggedIn", isLoggedIn);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "권한 확인 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



}




