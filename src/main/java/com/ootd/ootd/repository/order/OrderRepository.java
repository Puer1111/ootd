package com.ootd.ootd.repository.order;

import com.ootd.ootd.model.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
