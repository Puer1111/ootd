package com.ootd.ootd.service.clothes.impl;

import com.ootd.ootd.model.dto.clothes.ClothesDTO;
import com.ootd.ootd.model.entity.Clothes;
import com.ootd.ootd.repository.ClothesRepository;
import com.ootd.ootd.service.clothes.ClothesService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClothesServiceImpl implements ClothesService {

    @Autowired
    private ClothesRepository clothesRepository;

    // 카테고리별 상품 리스트 크롤링
    public List<ClothesDTO> crawlProductsByCategory(String category, int pageCount) {
        List<ClothesDTO> resultDTOs = new ArrayList<>();

        try {
            for (int page = 1; page <= pageCount; page++) {
                String url = "https://www.musinsa.com/categories/item/" + category + "?page=" + page;
                Document doc = getDocument(url);
                if (doc == null) continue;

                Elements productElements = doc.select(".li_box");
                for (Element productElement : productElements) {
                    ClothesDTO dto = parseProductElement(productElement, category);
                    if (dto != null) {
                        // DTO → Entity 변환 및 저장
                        Clothes entity = convertToEntity(dto);
                        Clothes savedEntity = clothesRepository.save(entity);
                        // Entity → DTO 변환하여 결과 리스트에 추가
                        resultDTOs.add(convertToDTO(savedEntity));
                    }
                }
                Thread.sleep(1500); // 차단 방지 딜레이
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultDTOs;
    }

    // 상품 상세 정보 크롤링
    public ClothesDTO crawlProductDetail(String productId) {
        try {
            String url = "https://www.musinsa.com/app/goods/" + productId;
            Document doc = getDocument(url);
            if (doc == null) return null;

            ClothesDTO dto = new ClothesDTO();
            dto.setId(productId);

            // 상품명 파싱
            Element nameElement = doc.selectFirst(".product_title");
            if (nameElement != null) {
                dto.setName(nameElement.text());
            }

            // 브랜드 파싱
            Element brandElement = doc.selectFirst(".product_title_brand");
            if (brandElement != null) {
                dto.setBrand(brandElement.text());
            }

            // 가격 파싱
            Element priceElement = doc.selectFirst(".price");
            if (priceElement != null) {
                String priceText = priceElement.text().replaceAll("[^0-9]", "");
                try {
                    dto.setPrice(Integer.parseInt(priceText));
                } catch (NumberFormatException e) {
                    dto.setPrice(0);
                }
            }

            // 이미지 URL 파싱
            Element imgElement = doc.selectFirst(".product-img img");
            if (imgElement != null) {
                dto.setImageUrl(imgElement.attr("src"));
            }

            // 옵션 파싱
            parseOptions(doc, dto);

            // DTO → Entity 변환 및 저장
            Clothes entity = convertToEntity(dto);
            Clothes savedEntity = clothesRepository.save(entity);

            // Entity → DTO 변환하여 반환
            return convertToDTO(savedEntity);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 전체 상품 조회
    public List<ClothesDTO> getAllClothes() {
        return clothesRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 카테고리별 상품 조회
    public List<ClothesDTO> getClothesByCategory(String category) {
        return clothesRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // HTTP 요청 및 HTML 문서 가져오기
    private Document getDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .referrer("https://www.google.com")
                    .timeout(5000)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 상품 목록에서 각 상품 요소 파싱
    private ClothesDTO parseProductElement(Element productElement, String category) {
        try {
            ClothesDTO dto = new ClothesDTO();
            dto.setCategory(category);

            // 상품 ID 파싱
            Element linkElement = productElement.selectFirst("a.img-block");
            if (linkElement != null) {
                String href = linkElement.attr("href");
                String[] parts = href.split("/");
                dto.setId(parts[parts.length - 1]);
            }

            // 상품명 파싱
            Element nameElement = productElement.selectFirst(".list_info a.item_title");
            if (nameElement != null) {
                dto.setName(nameElement.text());
            }

            // 가격 파싱
            Element priceElement = productElement.selectFirst(".price");
            if (priceElement != null) {
                String priceText = priceElement.text().replaceAll("[^0-9]", "");
                try {
                    dto.setPrice(Integer.parseInt(priceText));
                } catch (NumberFormatException e) {
                    dto.setPrice(0);
                }
            }

            // 이미지 URL 파싱
            Element imgElement = productElement.selectFirst("img.lazyload");
            if (imgElement != null) {
                dto.setImageUrl(imgElement.attr("data-original"));
                if (dto.getImageUrl().isEmpty()) {
                    dto.setImageUrl(imgElement.attr("src"));
                }
            }

            // 브랜드 파싱
            Element brandElement = productElement.selectFirst(".item_title");
            if (brandElement != null) {
                dto.setBrand(brandElement.text());
            }

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 상품 옵션 파싱
    private void parseOptions(Document doc, ClothesDTO dto) {
        // 사이즈 옵션 파싱
        Elements sizeElements = doc.select(".size_list li");
        if (!sizeElements.isEmpty()) {
            List<String> sizes = new ArrayList<>();
            for (Element sizeElement : sizeElements) {
                sizes.add(sizeElement.text().trim());
            }
            dto.getOptions().put("sizes", sizes);
        }

        // 색상 옵션 파싱
        Elements colorElements = doc.select(".color_list span");
        if (!colorElements.isEmpty()) {
            List<String> colors = new ArrayList<>();
            for (Element colorElement : colorElements) {
                colors.add(colorElement.attr("data-color"));
            }
            dto.getOptions().put("colors", colors);
        }
    }

    // DTO → Entity 변환
    private Clothes convertToEntity(ClothesDTO dto) {
        Clothes entity = new Clothes();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setBrand(dto.getBrand());
        entity.setPrice(dto.getPrice());
        entity.setImageUrl(dto.getImageUrl());
        entity.setCategory(dto.getCategory());

        // 옵션 변환
        if (dto.getOptions().containsKey("sizes")) {
            entity.setSizes(dto.getOptions().get("sizes"));
        }
        if (dto.getOptions().containsKey("colors")) {
            entity.setColors(dto.getOptions().get("colors"));
        }

        return entity;
    }

    // Entity → DTO 변환
    private ClothesDTO convertToDTO(Clothes entity) {
        ClothesDTO dto = new ClothesDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBrand(entity.getBrand());
        dto.setPrice(entity.getPrice());
        dto.setImageUrl(entity.getImageUrl());
        dto.setCategory(entity.getCategory());

        // 옵션 변환
        Map<String, List<String>> options = new HashMap<>();
        if (entity.getSizes() != null && !entity.getSizes().isEmpty()) {
            options.put("sizes", entity.getSizes());
        }
        if (entity.getColors() != null && !entity.getColors().isEmpty()) {
            options.put("colors", entity.getColors());
        }
        dto.setOptions(options);

        return dto;
    }
}