package com.ootd.ootd.repository.product;

import com.ootd.ootd.model.dto.product.AdminProductFlatDTO;
import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory , c.mainCategory) " +
            "FROM Brand b JOIN Product p ON p.brandNo = b.brandNo JOIN Category c ON c.categoryNo = p.categoryNo")
    List<ProductDTO> findAllandBrandName();

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b JOIN Product p ON p.brandNo = b.brandNo JOIN Category c ON c.categoryNo = p.categoryNo")
    List<ProductDTO> findAllWithMainCategory();

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductReview pr ON pr.productNo = p.productNo " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COUNT(pr.reviewId) DESC")
    List<ProductDTO> findAllOrderByReviewCountDesc();

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductReview pr ON pr.productNo = p.productNo " +
            "WHERE c.mainCategory = :mainCategory " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COUNT(pr.reviewId) DESC")
    List<ProductDTO> findByMainCategoryOrderByReviewCountDesc(@Param("mainCategory") String mainCategory);

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductReview pr ON pr.productNo = p.productNo " +
            "WHERE c.subCategory = :subCategory " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COUNT(pr.reviewId) DESC")
    List<ProductDTO> findBySubCategoryOrderByReviewCountDesc(@Param("subCategory") String subCategory);

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "JOIN ProductPromotion pp ON pp.productNo = p.productNo " +
            "WHERE pp.isRecommended = true " +
            "ORDER BY pp.promotionPriority DESC")
    List<ProductDTO> findRecommendedProducts();

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "JOIN ProductPromotion pp ON pp.productNo = p.productNo " +
            "WHERE pp.isSale = true " +
            "AND (pp.saleStartDate IS NULL OR pp.saleStartDate <= CURRENT_TIMESTAMP) " +
            "AND (pp.saleEndDate IS NULL OR pp.saleEndDate >= CURRENT_TIMESTAMP)")
    List<ProductDTO> findSaleProducts();

    @Query("SELECT DISTINCT c.mainCategory FROM Category c ORDER BY c.mainCategory")
    List<String> findDistinctMainCategories();

    @Query("SELECT DISTINCT c.subCategory FROM Category c WHERE c.mainCategory = :mainCategory ORDER BY c.subCategory")
    List<String> findSubCategoriesByMainCategory(@Param("mainCategory") String mainCategory);

    @Query("SELECT new com.ootd.ootd.model.dto.product.AdminProductFlatDTO(" +
            "p.productNo, p.productName, p.price, p.description, p.imageUrls, b.brandName, p.categoryNo, c.subCategory, " +
            "po.size, po.inventory, po.status, co.colorName , co.colorsNo) " +
            "FROM Product p " +
            "JOIN Brand b ON p.brandNo = b.brandNo " +
            "JOIN Category c ON p.categoryNo = c.categoryNo " +
            "LEFT JOIN ProductOption po ON p.productNo = po.productNo " +
            "LEFT JOIN Colors co ON po.colorNo = co.colorsNo")
    List<AdminProductFlatDTO> findAdminProducts();

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductLike pl ON pl.productNo = p.productNo " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COUNT(pl.productNo) DESC")
    List<ProductDTO> findAllOrderByLikeCountDesc();

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductLike pl ON pl.productNo = p.productNo " +
            "WHERE c.mainCategory = :mainCategory " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COUNT(pl.productNo) DESC")
    List<ProductDTO> findByMainCategoryOrderByLikeCountDesc(@Param("mainCategory") String mainCategory);

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductLike pl ON pl.productNo = p.productNo " +
            "WHERE c.subCategory = :subCategory " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COUNT(pl.productNo) DESC")
    List<ProductDTO> findBySubCategoryOrderByLikeCountDesc(@Param("subCategory") String subCategory);


    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductReview pr ON pr.productNo = p.productNo " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COALESCE(AVG(pr.rating), 0) DESC, COUNT(pr.reviewId) DESC")
    List<ProductDTO> findAllOrderByRatingDesc();

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductReview pr ON pr.productNo = p.productNo " +
            "WHERE c.mainCategory = :mainCategory " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COALESCE(AVG(pr.rating), 0) DESC, COUNT(pr.reviewId) DESC")
    List<ProductDTO> findByMainCategoryOrderByRatingDesc(@Param("mainCategory") String mainCategory);

    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b " +
            "JOIN Product p ON p.brandNo = b.brandNo " +
            "JOIN Category c ON c.categoryNo = p.categoryNo " +
            "LEFT JOIN ProductReview pr ON pr.productNo = p.productNo " +
            "WHERE c.subCategory = :subCategory " +
            "GROUP BY p.productNo, p.productName, p.price, p.description, b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory " +
            "ORDER BY COALESCE(AVG(pr.rating), 0) DESC, COUNT(pr.reviewId) DESC")
    List<ProductDTO> findBySubCategoryOrderByRatingDesc(@Param("subCategory") String subCategory);
}