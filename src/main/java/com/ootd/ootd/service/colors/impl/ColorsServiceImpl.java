package com.ootd.ootd.service.colors.impl;

import com.ootd.ootd.model.dto.colors.ColorsDTO;
import com.ootd.ootd.model.entity.colors.Colors;
import com.ootd.ootd.model.entity.product_colors.ProductColors;
import com.ootd.ootd.repository.colors.ColorsRepository;
import com.ootd.ootd.repository.product.ProductColorsRepository;
import com.ootd.ootd.service.colors.ColorsService;
import com.ootd.ootd.utils.RandomGenerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // 중복 체크: colorName이 이미 존재하는지 확인
        if (colorsRepository.findByColorName(dto.getColorName()).isPresent()) {
            throw new IllegalArgumentException(dto.getColorName() + "' already exists.");
        }

        Long colorsNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
        dto.setColorNo(colorsNo);
        Colors colorsEntity = ColorsDTO.convertToEntity(dto);
        colorsRepository.save(colorsEntity);
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
    @Override
    public ProductColors initToProductColor(List<Long> colorsNo) {
        // 1. 한 번에 모든 색상 조회 (성능 개선)
        List<Colors> colors = colorsRepository.findAllById(colorsNo);

        // 2. 조회된 색상 수와 요청한 색상 수가 같은지 확인
        if (colors.size() != colorsNo.size()) {
            List<Long> foundColorNos = colors.stream()
                    .map(Colors::getColorsNo)
                    .collect(Collectors.toList());
            List<Long> notFoundColors = colorsNo.stream()
                    .filter(colorNo -> !foundColorNos.contains(colorNo))
                    .collect(Collectors.toList());
            throw new RuntimeException("다음 색상을 찾을 수 없습니다: " + notFoundColors);
        }

        // 3. 조회된 색상들의 번호만 추출
        List<Long> validColorNos = colors.stream()
                .map(Colors::getColorsNo)
                .collect(Collectors.toList());

        // 4. 하나의 ProductColors에 모든 색상 번호 저장
        ProductColors productColors = new ProductColors(validColorNos);
        return productColorsRepository.save(productColors);
    }
}
