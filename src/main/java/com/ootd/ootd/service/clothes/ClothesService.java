package com.ootd.ootd.service.clothes;

import com.ootd.ootd.model.dto.clothes.ClothesDTO;

import java.util.List;

public interface ClothesService {
    public List<ClothesDTO> crawlProductsByCategory(String category, int pageCount);
    public ClothesDTO crawlProductDetail(String productId);
    public List<ClothesDTO> getAllClothes();
    public List<ClothesDTO> getClothesByCategory(String category);
}
