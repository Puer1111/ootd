package com.ootd.ootd.model.dto.clothes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@NoArgsConstructor
@Getter
@Setter
public class ClothesDTO {
    private String id;
    private String name;
    private String brand;
    private int price;
    private String imageUrl;
    private String category;
    private Map<String, List<String>> options = new HashMap<>();
}
