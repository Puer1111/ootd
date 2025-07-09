package com.ootd.ootd.model.entity.point;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 적립금 잔액 관리 엔티티
 * 경로: src/main/java/com/ootd/ootd/model/entity/point/UserPoint.java
 */
@Entity
@Table(name = "user_points")
public class UserPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_points", nullable = false)
    private Long totalPoints = 0L; // 총 적립금

    @Column(name = "available_points", nullable = false)
    private Long availablePoints = 0L; // 사용 가능한 적립금

    @Column(name = "used_points", nullable = false)
    private Long usedPoints = 0L; // 사용한 적립금

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public UserPoint() {}

    // 사용자 ID로 생성하는 생성자
    public UserPoint(Long userId) {
        this.userId = userId;
        this.totalPoints = 0L;
        this.availablePoints = 0L;
        this.usedPoints = 0L;
    }

    // 🔥 수정된 적립금 추가 메서드 (음수 처리 추가)
    public void addPoints(Long points) {
        if (points > 0) {
            // 양수: 적립
            this.totalPoints += points;
            this.availablePoints += points;
        } else if (points < 0) {
            // 음수: 차감 처리
            Long absPoints = Math.abs(points);
            if (this.availablePoints >= absPoints) {
                this.availablePoints -= absPoints;
                this.usedPoints += absPoints;
            } else {
                throw new IllegalArgumentException("사용 가능한 적립금이 부족합니다.");
            }
        }
    }

    // 적립금 사용 메서드
    public boolean usePoints(Long points) {
        if (this.availablePoints >= points) {
            this.availablePoints -= points;
            this.usedPoints += points;
            return true;
        }
        return false;
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

    public Long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Long getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(Long availablePoints) {
        this.availablePoints = availablePoints;
    }

    public Long getUsedPoints() {
        return usedPoints;
    }

    public void setUsedPoints(Long usedPoints) {
        this.usedPoints = usedPoints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "UserPoint{" +
                "id=" + id +
                ", userId=" + userId +
                ", totalPoints=" + totalPoints +
                ", availablePoints=" + availablePoints +
                ", usedPoints=" + usedPoints +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}