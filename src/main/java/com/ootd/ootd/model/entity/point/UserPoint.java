package com.ootd.ootd.model.entity.point;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    public UserPoint() {}

    public UserPoint(Long userId) {
        this.userId = userId;
        this.totalPoints = 0L;
        this.availablePoints = 0L;
        this.usedPoints = 0L;
    }

    public void addPoints(Long points) {
        if (points > 0) {
            this.totalPoints += points;
            this.availablePoints += points;
        } else if (points < 0) {
            Long absPoints = Math.abs(points);
            if (this.availablePoints >= absPoints) {
                this.availablePoints -= absPoints;
                this.usedPoints += absPoints;
            } else {
                throw new IllegalArgumentException("사용 가능한 적립금이 부족합니다.");
            }
        }
    }

    public boolean usePoints(Long points) {
        if (this.availablePoints >= points) {
            this.availablePoints -= points;
            this.usedPoints += points;
            return true;
        }
        return false;
    }

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