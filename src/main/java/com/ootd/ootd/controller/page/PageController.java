package com.ootd.ootd.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // 🔥 올바른 경로로 매핑
    @GetMapping("/recommended")
    public String recommendedPage() {
        return "view/promotion/recommended";
    }

    @GetMapping("/sale")
    public String salePage() {
        return "view/promotion/sale";
    }

}