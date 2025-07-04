package com.ootd.ootd.service.category.impl;

import com.ootd.ootd.model.dto.category.CategoryDTO;
import com.ootd.ootd.model.entity.category.Category;
import com.ootd.ootd.repository.category.CategoryRepository;
import com.ootd.ootd.service.category.CategoryService;
import com.ootd.ootd.utils.RandomGenerate;
import com.ootd.ootd.utils.StringToListConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryDTO registerCategory(CategoryDTO dto) {
        try {
            // 중복 체크: subCategory가 이미 존재하는지 확인
            if (categoryRepository.findBySubCategory(dto.getSubCategory()).isPresent()) {
                throw new IllegalArgumentException("SubCategory '" + dto.getSubCategory() + "' already exists.");
            }

            Long categoryNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
            dto.setCategoryNo(categoryNo);
            Category category = CategoryDTO.convertToEntity(dto);
            Category categoryEntity = categoryRepository.save(category);
            return CategoryDTO.convertToDTO(categoryEntity);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("categoryServiceImpl: " + e.getMessage());
            throw e; // 예외를 다시 던져서 상위 계층에서 처리할 수 있도록 함
        }
    }

//    @Override
//    @Transactional
//    public List<Map<String, Object>> getCategoryNoAndName() {
//        List<Map<String, Object>> categoryList = new ArrayList<>();
//        categoryRepository.mainCategory(mainCategory).forEach(category -> {
//            Map<String, Object> categoryMap = new HashMap<>();
//            categoryMap.put("categoryNo" , category[0]);
//            categoryMap.put("subCategory", category[1]);
//            categoryList.add(categoryMap);
//        });
//        return categoryList;
//    }

    @Override
    @Transactional
    public List<Map<String, Object>> findByMainCategory(String mainCategory) {
        List<Map<String, Object>> categoryList = new ArrayList<>();
        categoryRepository.findByMain(mainCategory).forEach(category -> {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("categoryNo" , category[0]);
            categoryMap.put("subCategory", category[1]);
            categoryList.add(categoryMap);
        });
        return categoryList;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
