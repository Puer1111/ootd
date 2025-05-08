package com.ootd.ootd.service.product.impl;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.entity.Product;
import com.ootd.ootd.repository.ProductRepository;
import com.ootd.ootd.service.product.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        List<Product> productList = productRepository.findByKeyword(keyword);
        return ProductDTO.fromEntityList(productList);
    }

    @Override
    @Transactional
    public ProductDTO insertProduct(ProductDTO dto) {
         Product productEntity = ProductDTO.convertToEntity(dto);
         Product product = productRepository.save(productEntity);
        return ProductDTO.convertToDTO(product);
    }
}