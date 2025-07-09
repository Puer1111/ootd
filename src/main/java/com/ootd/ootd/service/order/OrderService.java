package com.ootd.ootd.service.order;

import com.ootd.ootd.model.dto.order.OrderDTO;

public interface OrderService {

    /**
     * 새 주문 생성
     * @param order 주문 정보
     * @return 생성된 주문 정보
     */
    OrderDTO createOrder(OrderDTO order);

    /**
     * 주문 수량 및 총 금액 업데이트
     * @param orderId 주문 ID
     * @param quantity 새로운 수량 (Long 타입)
     * @param totalPrice 새로운 총 금액
     * @return 업데이트된 주문 정보
     */
    OrderDTO updateOrderQuantity(Long orderId, Long quantity, Long totalPrice);
}