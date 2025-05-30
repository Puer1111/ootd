package com.ootd.ootd.controller.payment;

import com.ootd.ootd.service.payment.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {

    @Autowired
    PaymentService paymentservice;

    public PaymentController(PaymentService paymentservice) {
        this.paymentservice = paymentservice;
    }

    @GetMapping("/goPay")
    public String goPay() {
        return "view/payment/payment";
    }


}
