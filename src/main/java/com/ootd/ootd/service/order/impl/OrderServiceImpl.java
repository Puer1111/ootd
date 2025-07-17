package com.ootd.ootd.service.order.impl;

import com.ootd.ootd.model.dto.order.OrderDTO;
import com.ootd.ootd.model.entity.order.Order;
import com.ootd.ootd.repository.order.OrderRepository;
import com.ootd.ootd.service.order.OrderService;
import com.ootd.ootd.service.payment.PaymentService;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // 💡 PaymentService 주입
    @Autowired
    private PaymentService paymentService;

    // 생성자
    public OrderServiceImpl(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    @Override
    public OrderDTO createOrder(OrderDTO dto) {
        Order savedOrder = OrderDTO.convertToEntity(dto);
        Order order = orderRepository.save(savedOrder);
        System.out.println("✅ 새 주문 생성 완료 - OrderID: " + order.getOrderId());
        return OrderDTO.convertToDTO(order);
    }

}