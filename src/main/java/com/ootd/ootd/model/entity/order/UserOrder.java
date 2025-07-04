package com.ootd.ootd.model.entity.order;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productNo;

    @Column(nullable = false)
    private Long userId;

    // 수량 필드 추가
    @Column(nullable = false)
    private Integer quantity = 1;

    // 총 주문 금액 필드 추가
    @Column(nullable = false)
    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ORDERED;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime cancelledAt;

    // 기본 생성자 (수량 1개)
    public UserOrder(Long productNo, Long userId) {
        this.productNo = productNo;
        this.userId = userId;
        this.quantity = 1;
        this.status = OrderStatus.ORDERED;
    }

    // 수량과 총 금액을 포함한 생성자
    public UserOrder(Long productNo, Long userId, Integer quantity, Long totalPrice) {
        this.productNo = productNo;
        this.userId = userId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.ORDERED;
    }

    // 주문 취소 메서드
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        ORDERED("주문완료"),
        CANCELLED("주문취소");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}