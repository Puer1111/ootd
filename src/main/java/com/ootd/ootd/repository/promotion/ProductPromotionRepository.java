package com.ootd.ootd.repository.promotion;

import com.ootd.ootd.model.entity.promotion.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Long> {

    // 특정 상품의 프로모션 정보 조회
    Optional<ProductPromotion> findByProductNo(Long productNo);

    // 추천 상품 목록 조회 (우선순위 순)
    List<ProductPromotion> findByIsRecommendedTrueOrderByPromotionPriorityDesc();

    // 세일 상품 목록 조회
    @Query("SELECT pp FROM ProductPromotion pp WHERE pp.isSale = true " +
            "AND (pp.saleStartDate IS NULL OR pp.saleStartDate <= :now) " +
            "AND (pp.saleEndDate IS NULL OR pp.saleEndDate >= :now)")
    List<ProductPromotion> findActiveSaleProducts(@Param("now") LocalDateTime now);

    // 추천 상품 개수 조회
    long countByIsRecommendedTrue();

    // 세일 상품 개수 조회
    @Query("SELECT COUNT(pp) FROM ProductPromotion pp WHERE pp.isSale = true " +
            "AND (pp.saleStartDate IS NULL OR pp.saleStartDate <= :now) " +
            "AND (pp.saleEndDate IS NULL OR pp.saleEndDate >= :now)")
    long countActiveSaleProducts(@Param("now") LocalDateTime now);

    // 여러 상품의 프로모션 정보 한번에 조회
    @Query("SELECT pp FROM ProductPromotion pp WHERE pp.productNo IN :productNos")
    List<ProductPromotion> findByProductNoIn(@Param("productNos") List<Long> productNos);

    // 만료된 세일 정리용 쿼리
    @Query("SELECT pp FROM ProductPromotion pp WHERE pp.isSale = true " +
            "AND pp.saleEndDate IS NOT NULL AND pp.saleEndDate < :now")
    List<ProductPromotion> findExpiredSaleProducts(@Param("now") LocalDateTime now);
}