package com.ootd.ootd.controller.order;

import com.ootd.ootd.model.dto.order.OrderDTO;
import com.ootd.ootd.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 새 주문 생성
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

    /**
     * 주문 수량 및 총 금액 업데이트
     */
    @PutMapping("/orders/update")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> updateRequest) {
        try {
            // 요청 데이터 파싱 (모두 Long 타입으로 통일)
            Long orderId = Long.parseLong(updateRequest.get("orderId").toString());
            Long quantity = Long.parseLong(updateRequest.get("quantity").toString());
            Long totalPrice = Long.parseLong(updateRequest.get("totalPrice").toString());

            System.out.println("🔄 주문 업데이트 요청 받음 - OrderID: " + orderId + ", Quantity: " + quantity + ", TotalPrice: " + totalPrice);

            // 서비스를 통해 주문 업데이트
            OrderDTO updatedOrder = orderService.updateOrderQuantity(orderId, quantity, totalPrice);

            System.out.println("✅ 주문 업데이트 응답 전송 - OrderID: " + updatedOrder.getOrderId());
            return ResponseEntity.ok(updatedOrder);

        } catch (NumberFormatException e) {
            System.err.println("❌ 잘못된 요청 데이터 형식: " + e.getMessage());
            return ResponseEntity.badRequest().body("잘못된 데이터 형식입니다.");
        } catch (Exception e) {
            System.err.println("❌ 주문 업데이트 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body("주문 업데이트에 실패했습니다: " + e.getMessage());
        }
    }
}