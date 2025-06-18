package com.ootd.ootd.service.brand.impl;

import com.ootd.ootd.model.dto.brand.BrandDTO;
import com.ootd.ootd.model.entity.brand.Brand;
import com.ootd.ootd.repository.brand.BrandRepository;
import com.ootd.ootd.service.brand.BrandService;
import com.ootd.ootd.utils.RandomGenerate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    public BrandServiceImpl(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    @Transactional
    public BrandDTO insertBrand(BrandDTO dto) {
//        Long BrandNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
//        dto.setBrandNo(BrandNo);
        try{
            Brand brandEntity = BrandDTO.convertToEntity(dto);
            Brand brand = brandRepository.save(brandEntity);
            return BrandDTO.convertToDTO(brand);
        }catch(Exception e){
            throw new RuntimeException();
        }
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getAllBrand() {
        List<Map<String, Object>> brandList = new ArrayList<>();
        brandRepository.findNoAndName().forEach(brand -> {
            Map<String, Object> brandMap = new HashMap<>();
            brandMap.put("brandNo", brand[1]);     // 첫 번째 값이 No
            brandMap.put("brandName", brand[0]);   // 두 번째 값이 Name
            brandList.add(brandMap);
        });
        return brandList;
    }
}
