package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.entity.productOption.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    @Modifying
    @Query("DELETE FROM ProductOption po WHERE po.productNo = :productNo")
    void deleteByProductNo(@Param("productNo") Long productNo);
}
