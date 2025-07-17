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
public interface ProductRepository extends JpaRepository<Product, String> {

    // 기존 쿼리
    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory , c.mainCategory) " +
            "FROM Brand b JOIN Product p ON p.brandNo = b.brandNo JOIN Category c ON c.categoryNo = p.categoryNo")
    List<ProductDTO> findAllandBrandName();

    // 🆕 메인 카테고리 포함 조회
    @Query("SELECT new com.ootd.ootd.model.dto.product.ProductDTO(" +
            "p.productNo, p.productName, p.price, p.description, " +
            "b.brandName, p.brandNo, p.imageUrls, p.categoryNo, c.subCategory, c.mainCategory) " +
            "FROM Brand b JOIN Product p ON p.brandNo = b.brandNo JOIN Category c ON c.categoryNo = p.categoryNo")
    List<ProductDTO> findAllWithMainCategory();

    // 🆕 리뷰 개수 기준 랭킹 (전체)
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

    // 🆕 메인 카테고리별 리뷰 랭킹
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

    // 🆕 하위 카테고리별 리뷰 랭킹
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

    // 🆕 추천 상품 조회
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

    // 🆕 세일 상품 조회
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

    // 🆕 메인 카테고리 목록 조회
    @Query("SELECT DISTINCT c.mainCategory FROM Category c ORDER BY c.mainCategory")
    List<String> findDistinctMainCategories();

    // 🆕 특정 메인 카테고리의 하위 카테고리 목록 조회
    @Query("SELECT DISTINCT c.subCategory FROM Category c WHERE c.mainCategory = :mainCategory ORDER BY c.subCategory")
    List<String> findSubCategoriesByMainCategory(@Param("mainCategory") String mainCategory);

    // 기존 관리자용 쿼리
    @Query("SELECT new com.ootd.ootd.model.dto.product.AdminProductFlatDTO(" +
            "p.productNo, p.productName, p.price, p.description, p.imageUrls, b.brandName, p.categoryNo, c.subCategory, " +
            "po.size, po.inventory, po.status, co.colorName) " +
            "FROM Product p " +
            "JOIN Brand b ON p.brandNo = b.brandNo " +
            "JOIN Category c ON p.categoryNo = c.categoryNo " +
            "LEFT JOIN ProductOption po ON p.productNo = po.productNo " +
            "LEFT JOIN Colors co ON po.colorNo = co.colorsNo")
    List<AdminProductFlatDTO> findAdminProducts();

    // ProductRepository.java 파일에 추가할 메서드들

    // ========== 🆕 좋아요 수 기준 정렬 메서드들 ==========

    // 전체 상품 좋아요 순 정렬
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

    // 메인 카테고리별 좋아요 순 정렬
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

    // 하위 카테고리별 좋아요 순 정렬
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

    // ========== 🆕 평점 기준 정렬 메서드들 ==========

    // 전체 상품 평점 순 정렬 (평점 높은 순, 평점 같으면 리뷰 많은 순)
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

    // 메인 카테고리별 평점 순 정렬
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

    // 하위 카테고리별 평점 순 정렬
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