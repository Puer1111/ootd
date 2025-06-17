package com.ootd.ootd.model.dto.category;

import com.ootd.ootd.model.entity.category.Category;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CategoryDTO {
    private Long categoryNo;
    private String mainCategory;
    private String subCategory;

    public static Category convertToEntity(CategoryDTO dto) {
        return Category.builder()
                .categoryNo(dto.getCategoryNo())
                .subCategory(dto.getSubCategory())
                .mainCategory(dto.getMainCategory())
                .build();
    }

    public static CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .categoryNo(category.getCategoryNo())
                .subCategory(category.getSubCategory())
                .mainCategory(category.getMainCategory())
                .build();
    }
}
