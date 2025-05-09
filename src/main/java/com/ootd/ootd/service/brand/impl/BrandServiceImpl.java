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
import java.util.List;

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
        Long BrandNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
        dto.setBrandNo(BrandNo);
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
    public List<String> getAllBrandNames() {
        List<String> brandNames = new ArrayList<>();
        brandRepository.findAll().forEach(brand -> brandNames.add(brand.getBrandName()));
        return brandNames;
    }
}
