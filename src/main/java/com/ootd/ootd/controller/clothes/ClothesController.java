package com.ootd.ootd.controller.clothes;

import com.ootd.ootd.model.dto.clothes.ClothesDTO;
import com.ootd.ootd.service.clothes.ClothesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

public class ClothesController {

    @GetMapping("/")
    public String test() {
        return "view/index";
    }

}




