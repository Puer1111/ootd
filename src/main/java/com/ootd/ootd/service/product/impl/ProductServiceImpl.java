package com.ootd.ootd.service.product.impl;

import com.ootd.ootd.model.dto.product.*;
import com.ootd.ootd.model.entity.product.Product;
import com.ootd.ootd.model.entity.productOption.ProductOption;
import com.ootd.ootd.repository.product.ProductLikeRepository;
import com.ootd.ootd.repository.product.ProductOptionRepository;
import com.ootd.ootd.repository.product.ProductRepository;
import com.ootd.ootd.repository.product.ProductReviewRepository;
import com.ootd.ootd.service.product.ProductService;
import com.ootd.ootd.model.dto.promotion.ProductPromotionDTO;
import com.ootd.ootd.service.promotion.ProductPromotionService;

import com.ootd.ootd.utils.RandomGenerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductPromotionService promotionService;

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

            List<ProductOption> productOptions = dto.getProductOption().stream()
                    .map(optionDto -> ProductOption.builder()
                            .productNo(productId)
                            .colorNo(optionDto.getColorsNo()) // ProductOptionDTO의 colorsNo 사용
                            .size(optionDto.getSize())
                            .inventory(optionDto.getInventory())
                            .status(optionDto.getStatus())
                            .build())
                    .collect(Collectors.toList());

            List<ProductOption> savedOption = productOptionRepository.saveAll(productOptions);

            return ProductDTO.convertToDTO(product);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        List<ProductDTO> productDTOs = productRepository.findAllandBrandName();


        return productDTOs.stream()
                .map(dto -> {
                    Long productNo = dto.getProductNo(); // 또는 dto.getId()

                    dto.setLikeCount(productLikeRepository.countByProductNo(productNo));
                    dto.setReviewCount(productReviewRepository.countByProductNo(productNo));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long productNo) {
        Product product = productRepository.findById(productNo)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        return ProductDTO.convertToDTO(product);
    }

    @Override
    @Transactional
    public ProductResponseInfoDTO updateProduct(Long productId, ProductResponseInfoDTO dto) {
        return productRepository.findById(productId)
                .map(existingProduct -> {
                    // 각 필드를 업데이트하기 전에 dto의 해당 필드가 null이 아닌지 확인
                    if (dto.getProductName() != null) {
                        existingProduct.setProductName(dto.getProductName());
                    }
                    if (dto.getPrice() != null) {
                        existingProduct.setPrice(dto.getPrice());
                    }
                    if (dto.getDescription() != null) {
                        existingProduct.setDescription(dto.getDescription());
                    }
                    if (dto.getBrandNo() != null) {
                        existingProduct.setBrandNo(dto.getBrandNo());
                    }
                    if (dto.getCategoryNo() != null) {
                        existingProduct.setCategoryNo(dto.getCategoryNo());
                    }
                    if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
                        existingProduct.setImageUrls(dto.getImageUrls());
                    }


                    if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                        List<ProductOption> productOptions = dto.getOptions().stream()
                                .map(optionDto -> ProductOption.builder()
                                        .optionId(null)
                                        .productNo(productId)
                                        .colorNo(optionDto.getColorsNo())
                                        .size(optionDto.getSize())
                                        .inventory(optionDto.getInventory())
                                        .status(optionDto.getStatus())
                                        .build())
                                .collect(Collectors.toList());
                        productOptionRepository.saveAll(productOptions);
                    }

                    Product updatedProduct = productRepository.save(existingProduct);
                    return ProductResponseInfoDTO.builder()
                            .productNo(updatedProduct.getProductNo())
                            .productName(updatedProduct.getProductName())
                            .description(updatedProduct.getDescription())
                            .price(updatedProduct.getPrice())
                            .brandNo(updatedProduct.getBrandNo())
                            .categoryNo(updatedProduct.getCategoryNo())
                            .imageUrls(updatedProduct.getImageUrls())
                            .options(dto.getOptions() != null ? dto.getOptions() : new ArrayList<>())
                            .build();
                })
                .orElseGet(() -> {
                    // 상품이 존재하지 않으면 새로 삽입 (기존 로직 유지)
                    ProductDTO newProductDTO = ProductDTO.builder()
                            .productName(dto.getProductName())
                            .price(dto.getPrice())
                            .description(dto.getDescription())
                            .brandNo(dto.getBrandNo())
                            .categoryNo(dto.getCategoryNo())
                            .isActiveSale(dto.getIsActiveSale())
                            .salePercentage(dto.getSalePercentage())
                            .imageUrls(dto.getImageUrls())
                            .productOption(dto.getOptions())
                            .build();

                    ProductDTO insertedProduct = insertProduct(newProductDTO);

                    return ProductResponseInfoDTO.builder()
                            .productNo(insertedProduct.getProductNo())
                            .productName(insertedProduct.getProductName())
                            .description(insertedProduct.getDescription())
                            .price(insertedProduct.getPrice())
                            .brandNo(insertedProduct.getBrandNo())
                            .categoryNo(insertedProduct.getCategoryNo())
                            .isActiveSale(insertedProduct.getIsActiveSale())
                            .salePercentage(insertedProduct.getSalePercentage())
                            .imageUrls(insertedProduct.getImageUrls())
                            .options(dto.getOptions() != null ? dto.getOptions() : new ArrayList<>())
                            .build();
                });
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        // 연관된 데이터 삭제
        productOptionRepository.deleteByProductNo(productId);
        productLikeRepository.deleteByProductNo(productId);
        productReviewRepository.deleteByProductNo(productId);

        // 상품 삭제
        productRepository.deleteById(productId);
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
                                    flatDto.getColorName() != null ? flatDto.getColorName() : "",
                                    flatDto.getColorsNo()
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

    @Override
    public List<ProductDTO> getSaleProducts() {
        try {
            System.out.println("🔥 세일 상품 조회 시작");

            List<ProductPromotionDTO> salePromotions = promotionService.getSaleProducts();
            System.out.println("세일 프로모션 개수: " + salePromotions.size());

            List<ProductDTO> saleProducts = new ArrayList<>();

            for (ProductPromotionDTO promotion : salePromotions) {
                try {
                    Product product = productRepository.findById(promotion.getProductNo())
                            .orElse(null);

                    if (product != null) {
                        ProductDTO productDTO = ProductDTO.convertToDTO(product);

                        productDTO.setPromotionInfo(promotion);

                        productDTO.setIsSale(true);
                        productDTO.setIsActiveSale(promotion.getIsActiveSale());
                        productDTO.setSalePercentage(promotion.getSalePercentage());

                        Integer originalPrice = productDTO.getPrice();
                        Integer salePrice = promotion.getSalePrice();

                        if (salePrice != null) {
                            productDTO.setSalePrice(salePrice);
                        } else if (promotion.getSalePercentage() != null && originalPrice != null) {
                            int calculatedSalePrice = originalPrice - (originalPrice * promotion.getSalePercentage() / 100);
                            productDTO.setSalePrice(calculatedSalePrice);
                        }

                        productDTO.setLikeCount(productLikeRepository.countByProductNo(product.getProductNo()));
                        productDTO.setReviewCount(productReviewRepository.countByProductNo(product.getProductNo()));

                        saleProducts.add(productDTO);
                        System.out.println("세일 상품 추가: " + productDTO.getProductName() +
                                " (원가: " + originalPrice + "원, 세일가: " + productDTO.getSalePrice() + "원)");
                    }
                } catch (Exception e) {
                    System.err.println("세일 상품 처리 실패 - productNo: " + promotion.getProductNo() + ", 에러: " + e.getMessage());
                }
            }

            System.out.println("✅ 최종 세일 상품 개수: " + saleProducts.size());
            return saleProducts;

        } catch (Exception e) {
            System.err.println("❌ 세일 상품 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}