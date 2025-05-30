package com.ootd.ootd.model.dto.brand;

import com.ootd.ootd.model.entity.brand.Brand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BrandDTO {
    private Long brandNo;
    private String brandName;
    private String brandLogoUrl;
    private String brandDescription;
    private String brandWebSite;

    public static Brand convertToEntity(BrandDTO dto) {
        Brand brand = new Brand();
        brand.setBrandNo(dto.getBrandNo());
        brand.setBrandName(dto.getBrandName());
        brand.setBrandLogoUrl(dto.getBrandLogoUrl());
        brand.setBrandWebsite(dto.getBrandWebSite());
        return brand;
    }

    public static BrandDTO convertToDTO(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setBrandNo(brand.getBrandNo());
        dto.setBrandName(brand.getBrandName());
        dto.setBrandLogoUrl(brand.getBrandLogoUrl());
        brand.setBrandWebsite(brand.getBrandWebsite());
        return dto;
    }
}
