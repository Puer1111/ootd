package com.ootd.ootd.service.brand;

import com.ootd.ootd.model.dto.brand.BrandDTO;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /**
     * 브랜드 등록
     *
     * @param dto
     * @return BrandDTO
     */
    BrandDTO insertBrand(BrandDTO dto);

    /**
     * 브랜드명 출력
     * @return List<String>
     */
    List<Map<String, Object>> getAllBrand();
}
