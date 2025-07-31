package com.ootd.ootd.repository.order;

import com.ootd.ootd.model.entity.order.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrderRepository extends JpaRepository<UserOrder, Long> {

    @Query("SELECT uo.productNo FROM UserOrder uo WHERE uo.userId = :userId AND uo.status = 'ORDERED' ORDER BY uo.createdAt DESC")
    List<Long> findProductNosByUserId(@Param("userId") Long userId);

    @Query("SELECT uo.productNo FROM UserOrder uo WHERE uo.userId = :userId AND uo.status = 'CANCELLED' ORDER BY uo.cancelledAt DESC")
    List<Long> findCancelledProductNosByUserId(@Param("userId") Long userId);

    int countByUserIdAndStatus(Long userId, UserOrder.OrderStatus status);

    boolean existsByUserIdAndProductNoAndStatus(Long userId, Long productNo, UserOrder.OrderStatus status);

    Optional<UserOrder> findByUserIdAndProductNoAndStatus(Long userId, Long productNo, UserOrder.OrderStatus status);

    List<UserOrder> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, UserOrder.OrderStatus status);

    Optional<UserOrder> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT uo FROM UserOrder uo WHERE uo.totalPrice = :totalPrice ORDER BY uo.createdAt DESC")
    Optional<UserOrder> findTopByTotalPriceOrderByCreatedAtDesc(@Param("totalPrice") Long totalPrice);

    @Query("SELECT uo FROM UserOrder uo ORDER BY uo.createdAt DESC")
    List<UserOrder> findTop5ByOrderByCreatedAtDesc();


    List<UserOrder> findByUserIdAndStatus(Long userId, UserOrder.OrderStatus status);

    List<UserOrder> findByUserId(long userId);

}