package com.ootd.ootd.controller.product;

import com.ootd.ootd.controller.colors.ColorsController;
import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.product_colors.ProductColors;
import com.ootd.ootd.service.colors.ColorsService;
import com.ootd.ootd.service.product.ProductService;
import com.ootd.ootd.utils.service.GoogleCloudStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    @Autowired
    ProductService productService;
    @Autowired
    ColorsService colorsService;
    @Autowired
    GoogleCloudStorageService googleCloudStorageService;

    public ProductController(ProductService productService, GoogleCloudStorageService googleCloudStorageService, ColorsService colorsService) {
        this.productService = productService;
        this.googleCloudStorageService = googleCloudStorageService;
        this.colorsService = colorsService;
    }

    @GetMapping("/")
    public String test() {
        return "view/index";
    }

//    @GetMapping("/search/products/{keyword}")
//    public List<ProductDTO> searchProducts(@PathVariable("keyword") String category) {
//        try {
//            return productService.searchProducts(category);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
    //TODO : 검색 구현해보기 .

    @GetMapping("/enter")
    public String showEnterProductForm(){
        return "view/product/enterProduct";
    }

    @PostMapping("/enter/product")
    public ResponseEntity<?> insertProduct(@ModelAttribute ProductDTO dto)  {
        ProductDTO productDTO;
        System.out.println("Inserted product : " + dto);
        // null 체크 추가
        if (dto.getImages() != null) {
            System.out.println("Images count: " + dto.getImages().length);
            for (int i = 0; i < dto.getImages().length; i++) {
                MultipartFile image = dto.getImages()[i];
                System.out.println("Image " + i + ": " + image.getOriginalFilename() +
                        ", Size: " + image.getSize());
            }
        }

        try {
            if (dto.getImages() != null && dto.getImages().length > 0) {
                // 배열 전체를 한 번에 전달
                List<String> images;
                images = googleCloudStorageService.uploadImages(dto.getImages());
                System.out.println("Uploaded " + dto.getImages().length + " images");
                dto.setImageUrls(images);
                System.out.println("Checked ImageUrl : " + images);
            } else {
                System.out.println("No images to upload");
            }

            // 상품색깔 테이블에 들어가는 데이터
            ProductColors productColors = colorsService.initToProductColor(dto.getColorsNo());
            dto.setProductColorsNo(productColors.getProductColorsNo());
            productDTO = productService.insertProduct(dto);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("product",productDTO);
        response.put("redirectUrl", "/");  // 마이페이지로 이동 엔드포인트 차후 수정.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}




