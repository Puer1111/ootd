package com.ootd.ootd.service.product.impl;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.product.Product;
import com.ootd.ootd.repository.product.ProductRepository;
import com.ootd.ootd.service.product.ProductService;

import com.ootd.ootd.utils.RandomGenerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

//    @Override
//    public List<ProductDTO> searchProducts(String category) {
//        List<Product> productList = productRepository.findByKeyword(category);
//        return ProductDTO.fromEntityList(productList);
//    }


    @Override
    @Transactional
    public ProductDTO insertProduct(ProductDTO dto) {
        Long productNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
        dto.setProductNo(productNo);
        try{
            Product productEntity = ProductDTO.convertToEntity(dto);
            Product product = productRepository.save(productEntity);
            return ProductDTO.convertToDTO(product);
        }catch(Exception e){
            throw new RuntimeException();
        }
    }

    // ProductServiceImpl에 추가
    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductDTO::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long productNo) {
        Product product = productRepository.findById(productNo.toString())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        return ProductDTO.convertToDTO(product);
    }
}