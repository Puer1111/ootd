package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.dto.product.AdminProductFlatDTO;
import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory) " +
            "FROM Brand b JOIN Product p ON p.brandNo = b.brandNo JOIN Category c ON c.categoryNo = p.categoryNo")

    List<ProductDTO> findAllandBrandName();

    @Query("SELECT new com.ootd.ootd.model.dto.product.AdminProductFlatDTO(" +
            "p.productNo, p.productName, p.price, p.description, p.imageUrls, b.brandName, p.categoryNo, c.subCategory, " +
            "po.size, po.inventory, po.status, co.colorName) " +
            "FROM Product p " +
            "JOIN Brand b ON p.brandNo = b.brandNo " +
            "JOIN Category c ON p.categoryNo = c.categoryNo " +
            "LEFT JOIN ProductOption po ON p.productNo = po.productNo " +
            "LEFT JOIN Colors co ON po.colorNo = co.colorsNo")
    List<AdminProductFlatDTO> findAdminProducts();



//    List<Product> findByKeyword(String category);
}
