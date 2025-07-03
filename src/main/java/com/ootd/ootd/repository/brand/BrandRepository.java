package com.ootd.ootd.repository.brand;

import com.ootd.ootd.model.entity.brand.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String> {
    @Query("SELECT b.brandName , b.brandNo from Brand b")
    List<Object[]> findNoAndName();

    Optional<Brand> findByBrandName(String brandName);
}
