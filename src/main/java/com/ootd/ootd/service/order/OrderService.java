package com.ootd.ootd.service.order;

import com.ootd.ootd.model.dto.order.OrderDTO;

public interface OrderService {
    OrderDTO createOrder(OrderDTO order);
}
