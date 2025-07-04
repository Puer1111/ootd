package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.entity.review.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    // 특정 상품의 리뷰 수 조회
    int countByProductNo(Long productNo);

    // 🆕 특정 사용자가 작성한 리뷰 수 조회 (마이페이지 후기 개수용)
    int countByUserId(Long userId);

    // 특정 상품의 평균 평점 조회
    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.productNo = :productNo")
    Double findAverageRatingByProductNo(@Param("productNo") Long productNo);

    // 특정 상품의 리뷰 목록 (최신순)
    List<ProductReview> findByProductNoOrderByCreatedAtDesc(Long productNo);

    // 사용자가 특정 상품에 리뷰를 작성했는지 확인
    boolean existsByProductNoAndUserId(Long productNo, Long userId);

    // 여러 상품의 리뷰 통계를 한번에 조회 (성능 최적화)
    @Query("SELECT pr.productNo as productNo, " +
            "COUNT(pr) as reviewCount, " +
            "COALESCE(AVG(pr.rating), 0) as avgRating " +
            "FROM ProductReview pr " +
            "WHERE pr.productNo IN :productNos " +
            "GROUP BY pr.productNo")
    List<Map<String, Object>> getReviewStatsByProductNos(@Param("productNos") List<Long> productNos);

    // 사용자가 작성한 리뷰 목록
    List<ProductReview> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT pr FROM ProductReview pr WHERE pr.productNo = :productNo ORDER BY pr.createdAt DESC")
    List<ProductReview> findTop10ByProductNoOrderByCreatedAtDesc(@Param("productNo") Long productNo, Pageable pageable);

}