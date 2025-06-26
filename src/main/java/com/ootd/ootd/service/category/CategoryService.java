package com.ootd.ootd.service.category;

import com.ootd.ootd.model.dto.category.CategoryDTO;
import com.ootd.ootd.model.entity.category.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    CategoryDTO registerCategory(CategoryDTO dto);

//    List<Map<String, Object>> getCategoryNoAndName();

    List<Map<String, Object>> findByMainCategory(String mainCategory);
}
