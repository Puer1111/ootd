package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.entity.product_colors.ProductColors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductColorsRepository extends JpaRepository<ProductColors, Long> {

}
