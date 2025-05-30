package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

//    List<Product> findByKeyword(String category);
}
