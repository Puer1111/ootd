package com.ootd.ootd.controller.clothes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClothesController {

    @GetMapping("/")
    public String index() {
        return "view/index";
    }
}
