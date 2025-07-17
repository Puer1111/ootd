package com.ootd.ootd.controller.coupon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/coupon")
public class CouponPageController {
    //    쿠폰 지급 페이지
    @GetMapping("/sale")
    public String getCouponPage(){
        return "view/coupon/coupon";
    }
}
