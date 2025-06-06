package com.ootd.ootd.service.product;

import com.ootd.ootd.model.dto.product.ProductDTO;

import java.util.List;


public interface ProductService {
    /**
     *  상품 검색
     * @param category
     * @return List<ClothesDTO>
     */
//    List<ProductDTO> searchProducts(String category);


    /**
     * 상품 입점
     *
     * @param dto
     * @return
     */
    ProductDTO insertProduct(ProductDTO dto);

    // ProductService 인터페이스에 추가
    List<ProductDTO> getAllProducts();
    ProductDTO getProductById(Long productNo);
}
