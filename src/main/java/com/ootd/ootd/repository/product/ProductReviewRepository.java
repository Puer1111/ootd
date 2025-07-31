package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.entity.review.ProductReview;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    int countByProductNo(Long productNo);

    int countByUserId(Long userId);

    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.productNo = :productNo")
    Double findAverageRatingByProductNo(@Param("productNo") Long productNo);

    List<ProductReview> findByProductNoOrderByCreatedAtDesc(Long productNo);

    boolean existsByProductNoAndUserId(Long productNo, Long userId);

    @Query("SELECT pr.productNo as productNo, " +
            "COUNT(pr) as reviewCount, " +
            "COALESCE(AVG(pr.rating), 0) as avgRating " +
            "FROM ProductReview pr " +
            "WHERE pr.productNo IN :productNos " +
            "GROUP BY pr.productNo")
    List<Map<String, Object>> getReviewStatsByProductNos(@Param("productNos") List<Long> productNos);

    List<ProductReview> findByUserIdOrderByCreatedAtDesc(Long userId);


    @Query("SELECT pr FROM ProductReview pr WHERE pr.productNo = :productNo ORDER BY pr.createdAt DESC")
    List<ProductReview> findTop10ByProductNoOrderByCreatedAtDesc(@Param("productNo") Long productNo, Pageable pageable);

    @Modifying
    void deleteByProductNo(Long productNo);

}