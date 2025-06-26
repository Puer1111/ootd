package com.ootd.ootd.service.product.impl;

import com.ootd.ootd.model.dto.product.ProductDTO;
import com.ootd.ootd.model.dto.product.ProductDetailDTO;
import com.ootd.ootd.model.dto.product.ProductOptionDTO;
import com.ootd.ootd.model.entity.product.Product;
import com.ootd.ootd.model.entity.productOption.ProductOption;
import com.ootd.ootd.repository.product.ProductLikeRepository;
import com.ootd.ootd.repository.product.ProductOptionRepository;
import com.ootd.ootd.repository.product.ProductRepository;
import com.ootd.ootd.repository.product.ProductReviewRepository;
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

    @Autowired
    private ProductLikeRepository productLikeRepository;        // 추가

    @Autowired
    private ProductReviewRepository productReviewRepository;    // 추가

    @Autowired
    private ProductOptionRepository productOptionRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductDTO insertProduct(ProductDTO dto) {
//        Long productNo = Long.parseLong(RandomGenerate.generateRandom10Digits());
//        dto.setProductNo(productNo);
        System.out.println("Null Check : " + dto);
        try {
            Product productEntity = ProductDTO.convertToEntity(dto);
            Product product = productRepository.save(productEntity);
            Long productId = product.getProductNo();

            List<ProductOption> productOptions = ProductOptionDTO.convertToEntityList(dto.getProductOption(), productId);

            // 🔥 변경: save → saveAll로 여러 옵션을 각각 별도 row로 저장

            List<ProductOption> savedOption = productOptionRepository.saveAll(productOptions);

            return ProductDTO.convertToDTO(product);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    // ProductServiceImpl에 추가
    @Override
    public List<ProductDTO> getAllProducts() {
        List<ProductDTO> productDTOs = productRepository.findAllandBrandName();


        return productDTOs.stream()
                .map(dto -> {
                    // DTO 변환 과정은 필요 없음 - 이미 DTO임
                    // ProductDTO dto = ProductDTO.convertToDTO(product); // 제거!

                    // ProductNo는 DTO에서 가져옴
                    Long productNo = dto.getProductNo(); // 또는 dto.getId() 등

                    // 좋아요 수와 리뷰 수만 추가 설정
                    dto.setLikeCount(productLikeRepository.countByProductNo(productNo));
                    dto.setReviewCount(productReviewRepository.countByProductNo(productNo));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long productNo) {
        Product product = productRepository.findById(productNo.toString())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        return ProductDTO.convertToDTO(product);
    }
}