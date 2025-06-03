package com.ootd.ootd.service.order.impl;

import com.ootd.ootd.model.dto.order.OrderDTO;
import com.ootd.ootd.model.entity.order.Order;
import com.ootd.ootd.repository.order.OrderRepository;
import com.ootd.ootd.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderDTO createOrder(OrderDTO dto) {
        Order savedOrder = OrderDTO.convertToEntity(dto);
        Order order = orderRepository.save(savedOrder);
        System.out.println("OrderId Entity 확인: " + order.getOrderId());
        return OrderDTO.convertToDTO(order);
    }
}
//TODO JS쪽 order에 정보 넘기는거 확인.