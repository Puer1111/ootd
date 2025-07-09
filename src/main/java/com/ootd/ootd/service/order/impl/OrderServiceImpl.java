package com.ootd.ootd.service.order.impl;

import com.ootd.ootd.model.dto.order.OrderDTO;
import com.ootd.ootd.model.entity.order.Order;
import com.ootd.ootd.repository.order.OrderRepository;
import com.ootd.ootd.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        System.out.println("✅ 새 주문 생성 완료 - OrderID: " + order.getOrderId());
        return OrderDTO.convertToDTO(order);
    }

    @Override
    public OrderDTO updateOrderQuantity(Long orderId, Long quantity, Long totalPrice) {
        try {
            System.out.println("🔄 주문 수량 업데이트 시작 - OrderID: " + orderId + ", Quantity: " + quantity + ", TotalPrice: " + totalPrice);

            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                throw new RuntimeException("주문을 찾을 수 없습니다: " + orderId);
            }

            Order order = orderOpt.get();
            order.setQuantity(quantity);
            order.setTotalPrice(totalPrice);

            Order updatedOrder = orderRepository.save(order);

            System.out.println("✅ 주문 수량 업데이트 완료 - OrderID: " + updatedOrder.getOrderId());

            return OrderDTO.convertToDTO(updatedOrder);

        } catch (Exception e) {
            System.err.println("❌ 주문 수량 업데이트 실패: " + e.getMessage());
            throw new RuntimeException("주문 수량 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
}
