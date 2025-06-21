package com.ootd.ootd.service.category;

import com.ootd.ootd.model.dto.category.CategoryDTO;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    CategoryDTO registerCategory(CategoryDTO dto);

    List<Map<String, Object>> getCategoryNoAndName();
}
