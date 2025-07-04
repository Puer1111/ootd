package com.ootd.ootd.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // 쿠폰 관리자 페이지
    @GetMapping("/coupon")
    public String adminCouponPage() {
        return "view/admin/coupon/adminCoupon";
    }
}
