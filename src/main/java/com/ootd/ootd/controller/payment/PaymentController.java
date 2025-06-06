package com.ootd.ootd.controller.payment;

import com.ootd.ootd.model.dto.payment.PaymentDTO;
import com.ootd.ootd.service.payment.PaymentService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/validation/{imp_uid}")
    @ResponseBody
    public IamportResponse<Payment> validatePayment(@PathVariable String imp_uid)
            throws IamportResponseException, IOException {
        return paymentservice.validatePayment(imp_uid);
    }

    @PostMapping("/payments/save")
    public ResponseEntity<?> savePayment(@RequestBody PaymentDTO dto) {
        System.out.println("DTO 확인: " + dto);
        try {
            paymentservice.savePayment(dto);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/getImpUid")
    public ResponseEntity<?> getImpUid(@RequestBody Map<String, Long> request) {
        Long orderId = request.get("orderNo");
        if (orderId == null) {
            return ResponseEntity.badRequest().body("orderNo is required");
        }
        String result = paymentservice.getImpUid(orderId);
        Map<String, String> response = new HashMap<>();
        response.put("impUid", result);

        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/payments/cancel/{imp_uid}")
    public ResponseEntity<?> cancelPayment(@PathVariable("imp_uid") String imp_uid) {
            IamportResponse<Payment> reponse = paymentservice.cancelPayment(imp_uid);
        return ResponseEntity.ok().build();
    }
}
