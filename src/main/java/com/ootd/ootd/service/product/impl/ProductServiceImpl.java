package com.ootd.ootd.service.product.impl;

import com.ootd.ootd.model.dto.product.AdminProductDTO;
import com.ootd.ootd.model.dto.product.AdminProductFlatDTO;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO dto) {
        Product product = productRepository.findById(String.valueOf(productId))
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 상품 정보 업데이트
        product.setProductName(dto.getProductName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        // ... 기타 필요한 필드 업데이트

        // 상품 옵션 업데이트 (기존 옵션 삭제 후 새로 추가하는 방식)
        productOptionRepository.deleteByProductNo(productId);
        List<ProductOption> productOptions = ProductOptionDTO.convertToEntityList(dto.getProductOption(), productId);
        productOptionRepository.saveAll(productOptions);

        Product updatedProduct = productRepository.save(product);
        return ProductDTO.convertToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        // 연관된 데이터 삭제
        productOptionRepository.deleteByProductNo(productId);
        productLikeRepository.deleteByProductNo(productId);
        productReviewRepository.deleteByProductNo(productId);

        // 상품 삭제
        productRepository.deleteById(String.valueOf(productId));
    }

    @Override
    public List<AdminProductDTO> getAdminProducts() {
        List<AdminProductFlatDTO> flatList = productRepository.findAdminProducts();

        // productNo를 기준으로 데이터를 그룹화하고, 순서를 유지하기 위해 LinkedHashMap 사용
        Map<Long, List<AdminProductFlatDTO>> groupedByProduct = flatList.stream()
                .collect(Collectors.groupingBy(
                        AdminProductFlatDTO::getProductNo,
                        LinkedHashMap::new, // 순서 보장을 위해 LinkedHashMap 사용
                        Collectors.toList()
                ));

        // 그룹화된 맵을 최종적인 AdminProductDTO 리스트로 변환
        return groupedByProduct.values().stream()
                .map(optionsForOneProduct -> {
                    // 리스트의 첫 번째 항목에서 공통 상품 정보를 가져옴
                    AdminProductFlatDTO firstOption = optionsForOneProduct.get(0);

                    // 해당 상품의 모든 옵션 정보를 ProductOptionInfo 리스트로 변환
                    List<AdminProductDTO.ProductOptionInfo> options = optionsForOneProduct.stream()
                            .map(flatDto -> new AdminProductDTO.ProductOptionInfo(
                                    flatDto.getSize() != null ? flatDto.getSize() : "",
                                    flatDto.getInventory() != null ? flatDto.getInventory() : 0,
                                    flatDto.getStatus() != null ? flatDto.getStatus() : "",
                                    flatDto.getColorName() != null ? flatDto.getColorName() : ""
                            ))
                            .collect(Collectors.toList());

                    // 최종적으로 계층 구조를 가진 AdminProductDTO를 빌드
                    return AdminProductDTO.builder()
                            .productNo(firstOption.getProductNo())
                            .productName(firstOption.getProductName())
                            .price(firstOption.getPrice())
                            .description(firstOption.getDescription())
                            .brandName(firstOption.getBrandName())
                            .categoryNo(firstOption.getCategoryNo())
                            .subCategory(firstOption.getSubCategory())
                            .imageUrls(firstOption.getImageUrls())
                            .options(options)
                            .build();
                })
                .collect(Collectors.toList());
    }
}