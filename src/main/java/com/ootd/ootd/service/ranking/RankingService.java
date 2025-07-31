package com.ootd.ootd.service.ranking;

import java.util.Map;

public interface RankingService {

    Map<String, Object> getProductRanking();

    Map<String, Object> getProductRankingByCategory(String mainCategory, String subCategory);

    Map<String, Object> getRecommendedProductRanking();

    Map<String, Object> getSaleProductRanking();

    Map<String, Object> getMainCategories();

    Map<String, Object> getSubCategories(String mainCategory);
}