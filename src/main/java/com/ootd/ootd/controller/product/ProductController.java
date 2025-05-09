package com.ootd.ootd.controller.product;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(final ProductService productService) {
        this.productService = productService;
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
    public ResponseEntity<?> insertProduct(@ModelAttribute ProductDTO dto){
        ProductDTO productDTO = productService.insertProduct(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("product",productDTO);
        response.put("redirectUrl", "/user/mypage");  // 마이페이지로 이동 엔드포인트 차후 수정.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}




