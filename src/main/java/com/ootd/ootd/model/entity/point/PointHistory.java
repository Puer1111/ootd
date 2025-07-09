package com.ootd.ootd.model.entity.point;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 적립금 내역 관리 엔티티
 * 경로: src/main/java/com/ootd/ootd/model/entity/point/PointHistory.java
 */
@Entity
@Table(name = "point_history")
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "points", nullable = false)
    private Long points; // 적립/사용 금액 (음수면 사용, 양수면 적립)

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false)
    private PointType pointType;

    @Column(name = "description", length = 500)
    private String description; // 적립/사용 사유

    @Column(name = "order_id")
    private Long orderId; // 관련 주문 ID (선택사항)

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 적립금 타입 열거형
     */
    public enum PointType {
        EARN_PURCHASE("구매 적립"),
        EARN_SIGNUP("가입 적립"),
        EARN_EVENT("이벤트 적립"),
        EARN_ADMIN("관리자 지급"),
        USE_PURCHASE("구매 사용"),
        USE_CANCEL("취소 환원"),
        EXPIRE("만료"),
        REFUND("환불");

        private final String description;

        PointType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 기본 생성자
    public PointHistory() {}

    // 적립용 생성자
    public PointHistory(Long userId, Long points, PointType pointType, String description) {
        this.userId = userId;
        this.points = points;
        this.pointType = pointType;
        this.description = description;
    }

    // 주문 관련 적립/사용용 생성자
    public PointHistory(Long userId, Long points, PointType pointType, String description, Long orderId) {
        this.userId = userId;
        this.points = points;
        this.pointType = pointType;
        this.description = description;
        this.orderId = orderId;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public PointType getPointType() {
        return pointType;
    }

    public void setPointType(PointType pointType) {
        this.pointType = pointType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PointHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", points=" + points +
                ", pointType=" + pointType +
                ", description='" + description + '\'' +
                ", orderId=" + orderId +
                ", createdAt=" + createdAt +
                '}';
    }
}