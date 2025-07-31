package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.entity.like.ProductLike;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    int countByProductNo(Long productNo);

    boolean existsByProductNoAndUserId(Long productNo, Long userId);

    Optional<ProductLike> findByProductNoAndUserId(Long productNo, Long userId);

    @Query("SELECT pl.productNo as productNo, COUNT(pl) as likeCount " +
            "FROM ProductLike pl " +
            "WHERE pl.productNo IN :productNos " +
            "GROUP BY pl.productNo")
    List<Map<String, Object>> countLikesByProductNos(@Param("productNos") List<Long> productNos);

    @Query("SELECT pl.productNo FROM ProductLike pl WHERE pl.userId = :userId ORDER BY pl.createdAt DESC")
    List<Long> findProductNosByUserId(@Param("userId") Long userId);

    int countByUserId(Long userId);


    List<ProductLike> findByUserId(Long userId);

    @Modifying
    void deleteByProductNo(Long productNo);
}