package com.ootd.ootd.model.entity.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @Column(name="category_no")
    private Long categoryNo;

    @Column(name = "main_Category" , nullable = false )
    private String mainCategory;

    @Column(name="sub_Category" , nullable = false)
    private String subCategory;

}
