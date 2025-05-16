package com.ootd.ootd.service.category;

import com.ootd.ootd.model.dto.category.CategoryDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO registerCategory(CategoryDTO dto);

    List<String> getAllCategoryNames();
}
