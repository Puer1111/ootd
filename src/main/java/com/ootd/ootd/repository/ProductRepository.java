package com.ootd.ootd.repository;

import com.ootd.ootd.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByKeyword(String keyword);
}
