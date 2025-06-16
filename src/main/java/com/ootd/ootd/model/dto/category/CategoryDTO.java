package com.ootd.ootd.model.dto.category;

import com.ootd.ootd.model.entity.category.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CategoryDTO {
    private Long categoryNo;
    private String categoryName;
    private String sizeGroup;

    public static Category convertToEntity(CategoryDTO dto) {
      Category category = new Category();
      category.setCategoryNo(dto.getCategoryNo());
      category.setCategoryName(dto.getCategoryName());
      category.setSizeGroup(dto.getSizeGroup());
      return category;
    }
    public static CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryNo(category.getCategoryNo());
        dto.setCategoryName(category.getCategoryName());
        dto.setSizeGroup(category.getSizeGroup());
        return dto;
    }
}
