package com.ootd.ootd.service.colors;

import com.ootd.ootd.model.dto.colors.ColorsDTO;
import com.ootd.ootd.model.entity.product_colors.ProductColors;

import java.util.List;
import java.util.Map;

public interface ColorsService {

    ColorsDTO registerColors(ColorsDTO dto);

    List<Map<String, Object>> lookupColors();

    ProductColors initToProductColor(List<Long> ProductColorsNo);
}
