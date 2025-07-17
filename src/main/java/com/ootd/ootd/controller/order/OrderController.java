package com.ootd.ootd.controller.order;

import com.ootd.ootd.model.dto.order.OrderDTO;
import com.ootd.ootd.model.entity.order.Order;
import com.ootd.ootd.model.entity.order.UserOrder;
import com.ootd.ootd.model.entity.user.User;
import com.ootd.ootd.repository.order.OrderRepository;
import com.ootd.ootd.repository.order.UserOrderRepository;
import com.ootd.ootd.repository.user.UserRepository;
import com.ootd.ootd.service.order.OrderService;
import com.ootd.ootd.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserOrderRepository userOrderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // 생성자 주입
    public OrderController(OrderService orderService, UserOrderRepository userOrderRepository, OrderRepository orderRepository, UserService userService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userOrderRepository = userOrderRepository;
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * 새 주문 생성 (Order + UserOrder 연결)
     */
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO order) {
        try {
            OrderDTO saveOrder = orderService.createOrder(order);
            System.out.println("✅ 주문 생성 성공 - OrderID: " + saveOrder.getOrderId());
            return ResponseEntity.ok(saveOrder);
        } catch (Exception e) {
            System.err.println("❌ 주문 생성 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body("주문 생성에 실패했습니다: " + e.getMessage());
        }
    }
}