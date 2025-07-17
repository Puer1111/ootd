package com.ootd.ootd.service.order;

import com.ootd.ootd.model.dto.order.OrderDTO;

import java.util.Map;

public interface OrderService {

    /**
     * 새 주문 생성
     * @param order 주문 정보
     * @return 생성된 주문 정보
     */
    OrderDTO createOrder(OrderDTO order);

}