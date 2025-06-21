package com.ootd.ootd.controller.category;

import com.ootd.ootd.model.dto.category.CategoryDTO;
import com.ootd.ootd.service.category.CategoryService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CategoryController {

    @Autowired
    public CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/api/register/category")
    public ResponseEntity<?> registerCategory(@RequestBody CategoryDTO dto) {

        try{
            CategoryDTO categoryDTO = categoryService.registerCategory(dto);
            return ResponseEntity.ok(categoryDTO);
        } catch (Exception e) {
            System.out.println("Insert cateGory error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/lookup/category")
        public ResponseEntity<?> lookupCategory() {
            try {
                List<Map<String, Object>> category = categoryService.getCategoryNoAndName();
                return ResponseEntity.ok(category);
            } catch(Exception e) {
                System.out.println("Insert cateGory error : " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", e.getMessage()));
            }
        }
}
