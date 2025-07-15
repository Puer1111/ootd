package com.ootd.ootd.model.entity.coupon;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 리스너 활성화
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id" ,nullable = false)
    private Long userCouponId;     // 식별자 ID

    @JoinColumn(name = "coupon_id" ,nullable = false)
    private Long couponId;              // 쿠폰 종류 식별 ID

    @Column(name = "user_id" ,nullable = false)
    private Long userId;                // 사용자 ID

    @CreatedDate // 엔티티 생성 시 자동으로 현재 시간 기록
    @Column(name = "received_at" ,nullable = false, updatable = false)
    private LocalDateTime receivedAt;   // 받은 날짜

    @Column(name = "is_used" )
    private Boolean isUsed = false;             // 사용 여부

    @Column(name = "used_at")
    private LocalDateTime usedAt;       // 사용 날짜

    @Column(name = "order_id")
    private Long orderId;               // 주문 상품 ID
}