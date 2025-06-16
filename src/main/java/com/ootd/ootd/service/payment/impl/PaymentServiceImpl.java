package com.ootd.ootd.service.payment.impl;

import com.ootd.ootd.model.dto.payment.PaymentDTO;
import com.ootd.ootd.model.entity.order.Order;
import com.ootd.ootd.repository.order.OrderRepository;
import com.ootd.ootd.repository.payment.PaymentRepository;
import com.ootd.ootd.service.payment.PaymentService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;

    private IamportClient iamportClient;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Value("${IMP_API_KEY}")
    String apiKey;
    @Value("${IMP_API_SECRET_KEY}")
    String secretKey;

    @Transactional
    @Override
    public IamportResponse<Payment> validatePayment(String imp_uid) {
        try {
            IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);
            System.out.println("Service Validate:" + payment);
            return payment;
        } catch (Exception e) {
            return null;
        }
    }
    @Transactional
    @Override
    public void savePayment(PaymentDTO dto) {
        com.ootd.ootd.model.entity.payment.Payment savedPayment = PaymentDTO.convertToEntity(dto);
        paymentRepository.save(savedPayment);
        orderRepository.changeOrderStatus(dto.getOrderId());
    }

    @Override
    public String getImpUid(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    @Transactional
    @Override
    public IamportResponse<Payment> cancelPayment(String imp_uid)  {
        try {
            CancelData cancelData = new CancelData(imp_uid, true);
            IamportResponse<Payment> payment = iamportClient.cancelPaymentByImpUid(cancelData);
            int result = paymentRepository.changeStatus(imp_uid);
            orderRepository.cancelOrderStatus(imp_uid);
            return payment;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
