package com.ootd.ootd.controller.product;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.dto.product.ProductOptionDTO;
import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.model.entity.like.ProductLike;
import com.ootd.ootd.model.entity.review.ProductReview;
import com.ootd.ootd.model.entity.user.User;
import com.ootd.ootd.model.entity.order.Order;
import com.ootd.ootd.model.entity.order.UserOrder;
import com.ootd.ootd.repository.product.ProductLikeRepository;
import com.ootd.ootd.repository.product.ProductReviewRepository;
import com.ootd.ootd.repository.user.UserRepository;
import com.ootd.ootd.repository.order.OrderRepository;
import com.ootd.ootd.repository.order.UserOrderRepository;
import com.ootd.ootd.service.colors.ColorsService;
import com.ootd.ootd.service.product.ProductService;
import com.ootd.ootd.service.promotion.ProductPromotionService;
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

import java.io.IOException;
import java.util.*;
import com.ootd.ootd.model.dto.product.ProductResponseInfoDTO;
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
    @Autowired
    private ProductPromotionService promotionService;
    @Autowired
    private OrderRepository orderRepository;

    public ProductController(ProductService productService, GoogleCloudStorageService googleCloudStorageService, ColorsService colorsService) {
        this.productService = productService;
        this.googleCloudStorageService = googleCloudStorageService;
        this.colorsService = colorsService;
    }

    // View 전달용 - 🆕 프로모션 정보 포함하도록 수정
    @GetMapping("/")
    public String showIndex(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        System.out.println("Total products fetched: " + products.size()); // 디버깅 로그 추가

        Map<String, List<ProductDTO>> productsByCategory = products.stream()
                .filter(p -> p.getMainCategory() != null && !p.getMainCategory().isEmpty())
                .collect(Collectors.groupingBy(ProductDTO::getMainCategory));

        System.out.println("Products grouped by category: " + productsByCategory.keySet()); // 디버깅 로그 추가

        model.addAttribute("productsByCategory", productsByCategory);
        return "view/index";
    }

    @GetMapping("/products/{productNo}")
    public String productDetail(@PathVariable Long productNo, Model model) {
        ProductDTO product = productService.getProductById(productNo);

        if (product != null) {
            ProductPromotionDTO promotion = promotionService.getPromotionByProductNo(productNo);
            if (promotion != null) {
                product.setPromotionInfo(promotion);
                System.out.println("상품 상세 페이지 - 프로모션 정보 포함: " + promotion);
            }
        }

        model.addAttribute("product", product);
        return "view/product/productDetail";
    }

    // JS 응답용 - 🆕 프로모션 정보 포함하도록 수정
    @GetMapping("/api/select/product/{productNo}")
    @ResponseBody
    public ResponseEntity<ProductDTO> getProductDetailsApi(@PathVariable Long productNo) {
        ProductDTO product = productService.getProductById(productNo);

        // 🆕 프로모션 정보 추가
        if (product != null) {
            ProductPromotionDTO promotion = promotionService.getPromotionByProductNo(productNo);
            if (promotion != null) {
                product.setPromotionInfo(promotion);
            }
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/insert/products")
    public ResponseEntity<?> insertProduct(@ModelAttribute ProductDTO dto, HttpServletRequest request) {
        ProductDTO productDTO;

        try {
            if (dto.getImages() != null && dto.getImages().length > 0) {
                List<String> images = googleCloudStorageService.uploadImages(dto.getImages());
                System.out.println("Uploaded " + dto.getImages().length + " images");
                dto.setImageUrls(images);
                System.out.println("Checked ImageUrl : " + images);
            } else {
                System.out.println("No images to upload");
            }

            productDTO = productService.insertProduct(dto);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("product", productDTO);
        response.put("redirectUrl", "/");  // 마이페이지로 이동 엔드포인트 차후 수정.

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable("productId") Long productNo,
            @ModelAttribute ProductResponseInfoDTO dto,
            @RequestParam(value = "size", required = false) List<String> sizes,
            @RequestParam(value = "inventory", required = false) List<Integer> inventories,
            @RequestParam(value = "colorsNo", required = false) List<Long> colorsNos,
            @RequestParam(value = "status", required = false) List<String> statuses,
            @RequestParam(value = "product.price[]", required = false) List<Integer> optionPrices
    ) {
        try {
            // 이미지 업로드 처리
            if (dto.getImages() != null && dto.getImages().length > 0) {
                List<String> images = googleCloudStorageService.uploadImages(dto.getImages());
                dto.setImageUrls(images);
            }

            // ProductOptionDTO 리스트를 수동으로 생성하여 dto에 설정
            List<ProductOptionDTO> options = new ArrayList<>();
            if (sizes != null && sizes.size() > 0) {
                for (int i = 0; i < sizes.size(); i++) {
                    ProductOptionDTO option = new ProductOptionDTO();
                    option.setSize(sizes.get(i));
                    if (colorsNos != null && i < colorsNos.size()) {
                        option.setColorsNo(colorsNos.get(i));
                    }
                    if (inventories != null && i < inventories.size()) {
                        option.setInventory(inventories.get(i));
                    }
                    if (statuses != null && i < statuses.size()) {
                        option.setStatus(statuses.get(i));
                    }
                    options.add(option);
                }
            }
            dto.setOptions(options); // ProductResponseInfoDTO의 options 필드에 설정

            ProductResponseInfoDTO updatedProduct = productService.updateProduct(productNo, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("product", updatedProduct);
            response.put("message", "상품이 성공적으로 수정되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return new ResponseEntity<>("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("상품 수정 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/admin/products/{productNo}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productNo) {
        try {
            productService.deleteProduct(productNo);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "상품이 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("상품 삭제 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                productLikeRepository.delete(existingLike.get());
                isLiked = false;
            } else {
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

            Integer quantity = 1;
            Long totalPrice = 0L;
            Integer unitPrice = 0;
            Boolean isActiveSale = false;
            Integer salePercentage = 0;

            if (orderRequest != null) {
                if (orderRequest.containsKey("quantity")) {
                    quantity = Integer.parseInt(orderRequest.get("quantity").toString());
                }
                if (orderRequest.containsKey("totalPrice")) {
                    totalPrice = Long.parseLong(orderRequest.get("totalPrice").toString());
                }
                if (orderRequest.containsKey("unitPrice")) {
                    unitPrice = Integer.parseInt(orderRequest.get("unitPrice").toString());
                }
                if (orderRequest.containsKey("isActiveSale")) {
                    isActiveSale = (Boolean) orderRequest.get("isActiveSale");
                }
                if (orderRequest.containsKey("salePercentage")) {
                    salePercentage = Integer.parseInt(orderRequest.get("salePercentage").toString());
                }
            }

            if (quantity < 1 || quantity > 99) {
                response.put("success", false);
                response.put("message", "수량은 1개 이상 99개 이하여야 합니다");
                return ResponseEntity.badRequest().body(response);
            }

            boolean alreadyOrdered = userOrderRepository.existsByUserIdAndProductNoAndStatus(
                    user.getId(), productNo, UserOrder.OrderStatus.ORDERED);

            if (alreadyOrdered) {
                response.put("success", false);
                response.put("message", "이미 주문하신 상품입니다");
                return ResponseEntity.badRequest().body(response);
            }

            ProductDTO product = productService.getProductById(productNo);
            if (product == null) {
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }

            ProductPromotionDTO promotion = promotionService.getPromotionByProductNo(productNo);
            if (promotion != null) {
                product.setPromotionInfo(promotion);
            }

            Long expectedTotalPrice;
            if (product.getIsActiveSale() != null && product.getIsActiveSale() && product.getSalePrice() != null) {
                expectedTotalPrice = (long) (product.getSalePrice() * quantity);
            } else {
                expectedTotalPrice = (long) (product.getPrice() * quantity);
            }

            if (totalPrice == 0L || !totalPrice.equals(expectedTotalPrice)) {
                totalPrice = expectedTotalPrice;
            }

            UserOrder order = new UserOrder(productNo, user.getId(), quantity, totalPrice);
            userOrderRepository.save(order);

            response.put("success", true);
            response.put("message", "주문이 완료되었습니다!");
            response.put("isOrdered", true);
            response.put("orderId", order.getId());
            response.put("quantity", quantity);
            response.put("totalPrice", totalPrice);
            response.put("isActiveSale", product.getIsActiveSale());
            response.put("salePercentage", product.getSalePercentage());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

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

    @GetMapping("/api/products/sale")
    @ResponseBody
    public ResponseEntity<?> getSaleProducts() {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("=== 세일 상품 API 호출 ===");

            List<ProductDTO> saleProducts = productService.getSaleProducts();
            System.out.println("세일 상품 개수: " + saleProducts.size());

            response.put("success", true);
            response.put("products", saleProducts);
            response.put("totalCount", saleProducts.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("세일 상품 API 실패: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "세일 상품을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            response.put("products", new ArrayList<>());
            response.put("totalCount", 0);

            return ResponseEntity.ok(response);
        }
    }

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
                    boolean hasOrdered = userOrderRepository.existsByUserIdAndProductNoAndStatus(
                            user.getId(), productNo, UserOrder.OrderStatus.ORDERED);

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