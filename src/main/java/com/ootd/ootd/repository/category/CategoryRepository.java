package com.ootd.ootd.repository.category;

import com.ootd.ootd.model.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c.categoryNo , c.subCategory from Category c")
    List<Object[]> findNoAndName();

}
