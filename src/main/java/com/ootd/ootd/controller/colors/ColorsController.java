package com.ootd.ootd.controller.colors;

import com.ootd.ootd.model.dto.colors.ColorsDTO;
import com.ootd.ootd.model.entity.colors.Colors;
import com.ootd.ootd.service.colors.ColorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ColorsController {

    @Autowired
    ColorsService colorsService;

    public ColorsController(ColorsService colorsService) {
        this.colorsService = colorsService;
    }

    @PostMapping("/api/register/colors")
    public ResponseEntity<?> registerColors(@RequestBody ColorsDTO dto) {
//        try{
            ColorsDTO savedDto = colorsService.registerColors(dto);  // 저장된 DTO 반환
            Map<String, Object> response = new HashMap<>();
            response.put("colorsNo", savedDto.getColorNo());
            response.put("colorName", savedDto.getColorName());
            return ResponseEntity.ok(response);  // 응답에 데이터 포함
//        }catch(Exception e){
//            return ResponseEntity.badRequest().body(dto);
//        }
    }
    @GetMapping("/api/lookup/colors")
    public ResponseEntity<?> lookupColors() {
        try {
            List<Map<String, Object>> colorList = colorsService.lookupColors();
            return ResponseEntity.ok(colorList);
        }catch(Exception e ){
            return ResponseEntity.badRequest().body(e);
        }

    }
}
