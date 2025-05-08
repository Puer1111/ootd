package com.ootd.ootd.service.product;

import com.ootd.ootd.model.dto.product.ProductDTO;

import java.util.List;


public interface ProductService {
    /**
     *  상품 검색
     * @param keyword
     * @return List<ClothesDTO>
     */
    List<ProductDTO> searchProducts(String keyword);


    /**
     * 상품 입점
     *
     * @param dto
     * @return
     */
    ProductDTO insertProduct(ProductDTO dto);
}
