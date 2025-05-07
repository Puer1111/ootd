package com.ootd.ootd.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "clothes")
@NoArgsConstructor
@Getter
@Setter
public class Clothes {

    @Id
    private String id;

    private String name;

    private String brand;

    private int price;

    private String imageUrl;

    private String category;

    @ElementCollection
    @CollectionTable(name = "clothes_sizes", joinColumns = @JoinColumn(name = "clothes_id"))
    @Column(name = "size")
    private List<String> sizes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "clothes_colors", joinColumns = @JoinColumn(name = "clothes_id"))
    @Column(name = "color")
    private List<String> colors = new ArrayList<>();

}