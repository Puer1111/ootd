package com.ootd.ootd.service.ranking;

import java.util.Map;

public interface RankingService {

    /**
     * 전체 상품 랭킹 조회 (리뷰 기준)
     */
    Map<String, Object> getProductRanking();

    /**
     * 카테고리별 상품 랭킹 조회
     */
    Map<String, Object> getProductRankingByCategory(String mainCategory, String subCategory);

    /**
     * 추천 상품 랭킹 조회
     */
    Map<String, Object> getRecommendedProductRanking();

    /**
     * 세일 상품 랭킹 조회
     */
    Map<String, Object> getSaleProductRanking();

    /**
     * 메인 카테고리 목록 조회
     */
    Map<String, Object> getMainCategories();

    /**
     * 하위 카테고리 목록 조회
     */
    Map<String, Object> getSubCategories(String mainCategory);
}