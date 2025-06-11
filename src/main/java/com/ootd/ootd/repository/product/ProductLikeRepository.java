package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.entity.like.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    // 특정 상품의 좋아요 수 조회
    int countByProductNo(Long productNo);

    // 사용자가 특정 상품에 좋아요 했는지 확인
    boolean existsByProductNoAndUserId(Long productNo, Long userId);

    // 사용자의 특정 상품 좋아요 찾기
    Optional<ProductLike> findByProductNoAndUserId(Long productNo, Long userId);

    // 여러 상품의 좋아요 수를 한번에 조회 (성능 최적화)
    @Query("SELECT pl.productNo as productNo, COUNT(pl) as likeCount " +
            "FROM ProductLike pl " +
            "WHERE pl.productNo IN :productNos " +
            "GROUP BY pl.productNo")
    List<Map<String, Object>> countLikesByProductNos(@Param("productNos") List<Long> productNos);

    // 사용자가 좋아요한 상품 목록
    List<ProductLike> findByUserId(Long userId);
}