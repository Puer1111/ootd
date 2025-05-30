package com.ootd.ootd.model.entity.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Category {

    @Id
    @Column(name="category_no")
    private Long categoryNo;

    @Column(name = "category_name" , nullable = false )
    private String categoryName;


}
