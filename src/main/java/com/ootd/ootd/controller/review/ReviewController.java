package com.ootd.ootd.controller.review;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.review.ProductReview;
import com.ootd.ootd.model.entity.user.User;
import com.ootd.ootd.repository.product.ProductReviewRepository;
import com.ootd.ootd.repository.user.UserRepository;
import com.ootd.ootd.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    // 내가 쓴 리뷰 페이지
    @GetMapping("/my-reviews")
    public String myReviewsPage() {
        return "view/product/review";
    }

    // 내가 쓴 리뷰 목록 API
    @GetMapping("/my-reviews-data")
    @ResponseBody
    public ResponseEntity<?> getMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // 내가 작성한 리뷰 목록 조회
            List<ProductReview> myReviews = productReviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

            List<Map<String, Object>> reviewsWithProduct = new ArrayList<>();

            for (ProductReview review : myReviews) {
                ProductDTO product = productService.getProductById(review.getProductNo());
                if (product != null) {
                    Map<String, Object> reviewWithProduct = new HashMap<>();

                    // 리뷰 정보
                    reviewWithProduct.put("id", review.getReviewId()); // getId() -> getReviewId()
                    reviewWithProduct.put("productNo", review.getProductNo());
                    reviewWithProduct.put("rating", review.getRating());
                    reviewWithProduct.put("content", review.getContent());
                    reviewWithProduct.put("createdAt", review.getCreatedAt());

                    // 상품 정보
                    reviewWithProduct.put("productName", product.getProductName());
                    reviewWithProduct.put("productImageUrls", product.getImageUrls());
                    reviewWithProduct.put("productPrice", product.getPrice());

                    reviewsWithProduct.add(reviewWithProduct);
                }
            }

            response.put("success", true);
            response.put("reviews", reviewsWithProduct);
            response.put("totalCount", reviewsWithProduct.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "리뷰 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 리뷰 삭제 API
    @DeleteMapping("/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> deleteMyReview(@PathVariable Long reviewId,
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

            // 리뷰 존재 확인 및 본인 리뷰인지 확인
            Optional<ProductReview> reviewOpt = productReviewRepository.findById(reviewId);

            if (reviewOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "리뷰를 찾을 수 없습니다");
                return ResponseEntity.badRequest().body(response);
            }

            ProductReview review = reviewOpt.get();
            if (!review.getUserId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "본인이 작성한 리뷰만 삭제할 수 있습니다");
                return ResponseEntity.badRequest().body(response);
            }

            // 리뷰 삭제
            productReviewRepository.delete(review);

            response.put("success", true);
            response.put("message", "리뷰가 삭제되었습니다");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "리뷰 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}