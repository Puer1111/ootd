package com.ootd.ootd.repository;

import com.ootd.ootd.model.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, String> {
    // 카테고리별 상품 조회
    List<Clothes> findByCategory(String category);

    // 브랜드별 상품 조회
    List<Clothes> findByBrand(String brand);

    // 가격 범위별 상품 조회
    List<Clothes> findByPriceBetween(int minPrice, int maxPrice);
}
