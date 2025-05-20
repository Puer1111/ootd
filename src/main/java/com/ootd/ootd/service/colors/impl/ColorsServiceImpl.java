package com.ootd.ootd.service.colors.impl;

import com.ootd.ootd.model.dto.colors.ColorsDTO;
import com.ootd.ootd.model.entity.colors.Colors;
import com.ootd.ootd.model.entity.product_colors.ProductColors;
import com.ootd.ootd.repository.colors.ColorsRepository;
import com.ootd.ootd.repository.product_colors.ProductColorsRepository;
import com.ootd.ootd.service.colors.ColorsService;
import com.ootd.ootd.utils.RandomGenerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ColorsServiceImpl implements ColorsService {
    @Autowired
    ColorsRepository colorsRepository;
    @Autowired
    ProductColorsRepository productColorsRepository;

    public ColorsServiceImpl(ColorsRepository colorsRepository, ProductColorsRepository productColorsRepository) {
        this.colorsRepository = colorsRepository;
        this.productColorsRepository = productColorsRepository;
    }

    @Override
    public ColorsDTO registerColors(ColorsDTO dto) {
        Long colorsNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
        dto.setColorNo(colorsNo);
        Colors colorsEntity = ColorsDTO.convertToEntity(dto);
        colorsRepository.save(colorsEntity);
//        return dto;
        return dto;
    }

    @Override
    public  List<Map<String, Object>> lookupColors() {
        List<Map<String, Object>> result = new ArrayList<>();
        colorsRepository.findAll().forEach(color -> {
            Map<String, Object> colorMap = new HashMap<>();
            colorMap.put("colorNo", color.getColorsNo());
            colorMap.put("colorName", color.getColorName());
            result.add(colorMap);
        });
        return result;
    }
    // 상품 색깔 데이터
    @Override
    public ProductColors initToProductColor(List<Long> ColorsNo) {
//        Long productColorsNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
        ProductColors result = null;
        for (Long colorNo : ColorsNo) {
            // Colors 엔티티 조회
            Colors checkedColorNo = colorsRepository.findById(colorNo)
                    .orElseThrow(() -> new RuntimeException("색상을 찾을 수 없습니다: " + colorNo));
            ProductColors productColors = new ProductColors(checkedColorNo);
            productColorsRepository.save(productColors);
            result = productColors;
        }
        return result;
    }
}
