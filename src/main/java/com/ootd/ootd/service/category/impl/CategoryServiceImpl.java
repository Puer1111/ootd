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

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    //TODO 등록시 : 중복 체크 확인 안했음.
    public CategoryDTO registerCategory(CategoryDTO dto) {
        try{
            Long categoryNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
            dto.setCategoryNo(categoryNo);
            Category category = CategoryDTO.convertToEntity(dto);
            Category categoryEntity = categoryRepository.save(category);
            return CategoryDTO.convertToDTO(categoryEntity);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("categoryServiceImpl: " + e.getMessage());
            return null;
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
}
