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
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
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

    @Override
    public List<String> getAllCategoryNames() {
        List<String> categoryNames = new ArrayList<>();
        categoryRepository.findAll().forEach(category -> categoryNames.add(category.getCategoryName()));
        return categoryNames;
    }
}
