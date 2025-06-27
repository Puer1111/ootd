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

    // 사용자의 주문한 상품 번호 목록 (주문완료만, 최신순)
    @Query("SELECT uo.productNo FROM UserOrder uo WHERE uo.userId = :userId AND uo.status = 'ORDERED' ORDER BY uo.createdAt DESC")
    List<Long> findProductNosByUserId(@Param("userId") Long userId);

    // 사용자의 취소한 상품 번호 목록 (최신순)
    @Query("SELECT uo.productNo FROM UserOrder uo WHERE uo.userId = :userId AND uo.status = 'CANCELLED' ORDER BY uo.cancelledAt DESC")
    List<Long> findCancelledProductNosByUserId(@Param("userId") Long userId);

    // 사용자 주문 수 (주문완료만)
    int countByUserIdAndStatus(Long userId, UserOrder.OrderStatus status);

    // 특정 사용자가 특정 상품을 주문했는지 확인 (주문완료만)
    boolean existsByUserIdAndProductNoAndStatus(Long userId, Long productNo, UserOrder.OrderStatus status);

    // 사용자의 특정 상품 주문 찾기 (주문완료만)
    Optional<UserOrder> findByUserIdAndProductNoAndStatus(Long userId, Long productNo, UserOrder.OrderStatus status);

    // 사용자의 모든 주문 목록 (상태별)
    List<UserOrder> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, UserOrder.OrderStatus status);

    // 주문 ID로 찾기
    Optional<UserOrder> findByIdAndUserId(Long id, Long userId);
}