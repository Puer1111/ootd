package com.ootd.ootd.service.ranking.impl;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.repository.product.ProductRepository;
import com.ootd.ootd.repository.product.ProductLikeRepository;
import com.ootd.ootd.repository.product.ProductReviewRepository;
import com.ootd.ootd.repository.promotion.ProductPromotionRepository;
import com.ootd.ootd.service.ranking.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductPromotionRepository promotionRepository;

    @Override
    public Map<String, Object> getProductRanking() {
        List<ProductDTO> products = productRepository.findAllOrderByReviewCountDesc();

        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("sortType", "reviews");

        return result;
    }

    @Override
    public Map<String, Object> getProductRankingByCategory(String mainCategory, String subCategory) {
        List<ProductDTO> products;

        String mappedMainCategory = mapCategoryToKorean(mainCategory);

        if (subCategory != null && !subCategory.trim().isEmpty() && !"all".equals(subCategory)) {
            products = productRepository.findBySubCategoryOrderByReviewCountDesc(subCategory);
        } else if (mappedMainCategory != null && !mappedMainCategory.trim().isEmpty() && !"all".equals(mappedMainCategory)) {
            products = productRepository.findByMainCategoryOrderByReviewCountDesc(mappedMainCategory);
        } else {
            products = productRepository.findAllOrderByReviewCountDesc();
        }

        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("filter", Map.of(
                "mainCategory", mainCategory != null ? mainCategory : "all",
                "subCategory", subCategory != null ? subCategory : "all"
        ));

        return result;
    }

    @Override
    public Map<String, Object> getRecommendedProductRanking() {
        List<ProductDTO> products = productRepository.findRecommendedProducts();

        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("type", "recommended");

        return result;
    }

    @Override
    public Map<String, Object> getSaleProductRanking() {
        List<ProductDTO> products = productRepository.findSaleProducts();

        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("type", "sale");

        return result;
    }

    @Override
    public Map<String, Object> getMainCategories() {
        List<String> categories = productRepository.findDistinctMainCategories();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("categories", categories);

        return result;
    }

    @Override
    public Map<String, Object> getSubCategories(String mainCategory) {
        String mappedCategory = mapCategoryToKorean(mainCategory);
        List<String> categories = productRepository.findSubCategoriesByMainCategory(mappedCategory);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("categories", categories);
        result.put("mainCategory", mainCategory);

        return result;
    }


    public Map<String, Object> getProductRankingBySortType(String sortBy) {
        List<ProductDTO> products;

        switch (sortBy.toLowerCase()) {
            case "likes":
                products = productRepository.findAllOrderByLikeCountDesc();
                break;
            case "rating":
                products = productRepository.findAllOrderByRatingDesc();
                break;
            case "reviews":
            default:
                products = productRepository.findAllOrderByReviewCountDesc();
                break;
        }

        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("sortType", sortBy);

        return result;
    }

    public Map<String, Object> getProductRankingByCategoryAndSortType(String mainCategory, String subCategory, String sortBy) {
        List<ProductDTO> products = getProductsByCategory(mainCategory, subCategory, sortBy);

        List<ProductDTO> enrichedProducts = products.stream()
                .map(this::enrichProductWithStats)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("products", enrichedProducts);
        result.put("totalCount", enrichedProducts.size());
        result.put("sortType", sortBy);
        result.put("filter", Map.of(
                "mainCategory", mainCategory != null ? mainCategory : "all",
                "subCategory", subCategory != null ? subCategory : "all"
        ));

        return result;
    }

    private List<ProductDTO> getProductsByCategory(String mainCategory, String subCategory, String sortType) {
        String mappedMainCategory = mapCategoryToKorean(mainCategory);

        if (subCategory != null && !subCategory.trim().isEmpty() && !"all".equals(subCategory)) {
            switch (sortType.toLowerCase()) {
                case "likes":
                    return productRepository.findBySubCategoryOrderByLikeCountDesc(subCategory);
                case "rating":
                    return productRepository.findBySubCategoryOrderByRatingDesc(subCategory);
                case "reviews":
                default:
                    return productRepository.findBySubCategoryOrderByReviewCountDesc(subCategory);
            }
        } else if (mappedMainCategory != null && !mappedMainCategory.trim().isEmpty() && !"all".equals(mappedMainCategory)) {
            switch (sortType.toLowerCase()) {
                case "likes":
                    return productRepository.findByMainCategoryOrderByLikeCountDesc(mappedMainCategory);
                case "rating":
                    return productRepository.findByMainCategoryOrderByRatingDesc(mappedMainCategory);
                case "reviews":
                default:
                    return productRepository.findByMainCategoryOrderByReviewCountDesc(mappedMainCategory);
            }
        } else {
            switch (sortType.toLowerCase()) {
                case "likes":
                    return productRepository.findAllOrderByLikeCountDesc();
                case "rating":
                    return productRepository.findAllOrderByRatingDesc();
                case "reviews":
                default:
                    return productRepository.findAllOrderByReviewCountDesc();
            }
        }
    }

    private String mapCategoryToKorean(String englishCategory) {
        if (englishCategory == null || "all".equals(englishCategory)) {
            return null;
        }

        switch (englishCategory.toLowerCase()) {
            case "top":
                return "상의";
            case "bottom":
                return "하의";
            case "shoes":
                return "신발";
            default:
                return englishCategory;
        }
    }

    private ProductDTO enrichProductWithStats(ProductDTO product) {
        Long productNo = product.getProductNo();

        int likeCount = productLikeRepository.countByProductNo(productNo);
        product.setLikeCount(likeCount);

        int reviewCount = productReviewRepository.countByProductNo(productNo);
        product.setReviewCount(reviewCount);

        Double averageRating = productReviewRepository.findAverageRatingByProductNo(productNo);
        product.setAverageRating(averageRating != null ? averageRating : 0.0);

        promotionRepository.findByProductNo(productNo).ifPresent(promotion -> {
            ProductPromotionDTO promotionDTO = ProductPromotionDTO.convertToDTO(promotion);
            product.setPromotionInfo(promotionDTO);
        });

        return product;
    }
}