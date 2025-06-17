package com.ootd.ootd.controller.brand;

import com.ootd.ootd.model.dto.brand.BrandDTO;
import com.ootd.ootd.service.brand.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class BrandController {

    @Autowired
    BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/api/register/brands")
    public ResponseEntity<?> insertBrand(@RequestBody BrandDTO dto) {
        try {
            BrandDTO brandDto = brandService.insertBrand(dto);
            System.out.println("brandName = : " + dto.getBrandName());
            return ResponseEntity.ok(brandDto);
        } catch (Exception e) {
            System.out.println("Insert brand error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/api/lookup/brands")
    public ResponseEntity<?> selectBrands() {
        try{
            List<String> brandNames = brandService.getAllBrand();
            return ResponseEntity.ok(brandNames);
        }catch(Exception e){
            System.out.println("Select brands error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));        }
    }
}
