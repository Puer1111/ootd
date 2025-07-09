package com.ootd.ootd.repository.order;

import com.ootd.ootd.model.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrderRepository extends JpaRepository<Order, Long> {
   @Modifying
   @Transactional
   @Query("UPDATE Order o SET o.orderStatus = 'success' WHERE o.orderId=:orderId")
    void changeOrderStatus(Long orderId);

   @Modifying
   @Transactional
   @Query("UPDATE Order o " +
           "SET o.orderStatus = 'cancel' " +
           "WHERE o.orderId IN (" +
           "    SELECT p.orderId FROM Payment p WHERE p.impUid = :impUid" +
           ")")
    void cancelOrderStatus(String impUid);

    @Query("SELECT o.orderStatus FROM Order o WHERE o.merchantUid = (SELECT p.merchantUid FROM Payment p WHERE p.impUid = :impUid)")
    String findOrderStatusByImpUid(@Param("impUid") String impUid);
}
