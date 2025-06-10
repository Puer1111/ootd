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
        String[] rawColors = request.getParameterValues("colorsNo");
        ProductDTO productDTO;

        try {
            if (rawColors != null && rawColors.length > 0) {
                List<Long> colorsNoList = Arrays.stream(rawColors)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                dto.setColorsNo(colorsNoList);
            }

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
            ProductColors productColors = colorsService.initToProductColor(dto.getColorsNo());
            dto.setProductColorsNo(productColors.getProductColorsNo());
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



}




