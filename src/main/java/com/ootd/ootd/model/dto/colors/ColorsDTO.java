package com.ootd.ootd.model.dto.colors;

import com.ootd.ootd.model.entity.colors.Colors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ColorsDTO {
    private Long colorNo;
    private String colorName;

    public static Colors convertToEntity(ColorsDTO dto){
        Colors colors = new Colors();
        colors.setColorName(dto.getColorName());
        colors.setColorsNo(dto.getColorNo());
        return colors;
    }

    public static ColorsDTO convertToDTO(Colors colors){
        ColorsDTO colorsDTO = new ColorsDTO();
        colorsDTO.setColorName(colors.getColorName());
        colorsDTO.setColorNo(colors.getColorsNo());
        return colorsDTO;
    }
}
