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

    Optional<ProductPromotion> findByProductNo(Long productNo);

    List<ProductPromotion> findByIsRecommendedTrueOrderByPromotionPriorityDesc();

    @Query("SELECT pp FROM ProductPromotion pp WHERE pp.isSale = true " +
            "AND (pp.saleStartDate IS NULL OR pp.saleStartDate <= :now) " +
            "AND (pp.saleEndDate IS NULL OR pp.saleEndDate >= :now)")
    List<ProductPromotion> findActiveSaleProducts(@Param("now") LocalDateTime now);

    long countByIsRecommendedTrue();

    @Query("SELECT COUNT(pp) FROM ProductPromotion pp WHERE pp.isSale = true " +
            "AND (pp.saleStartDate IS NULL OR pp.saleStartDate <= :now) " +
            "AND (pp.saleEndDate IS NULL OR pp.saleEndDate >= :now)")
    long countActiveSaleProducts(@Param("now") LocalDateTime now);

    @Query("SELECT pp FROM ProductPromotion pp WHERE pp.productNo IN :productNos")
    List<ProductPromotion> findByProductNoIn(@Param("productNos") List<Long> productNos);

    @Query("SELECT pp FROM ProductPromotion pp WHERE pp.isSale = true " +
            "AND pp.saleEndDate IS NOT NULL AND pp.saleEndDate < :now")
    List<ProductPromotion> findExpiredSaleProducts(@Param("now") LocalDateTime now);
}