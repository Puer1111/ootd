package com.ootd.ootd.model.dto.point;

import com.ootd.ootd.model.entity.point.UserPoint;

import java.time.LocalDateTime;

public class UserPointDTO {

    private Long id;
    private Long userId;
    private Long totalPoints;
    private Long availablePoints;
    private Long usedPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserPointDTO() {}

    public UserPointDTO(Long id, Long userId, Long totalPoints, Long availablePoints,
                        Long usedPoints, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.totalPoints = totalPoints;
        this.availablePoints = availablePoints;
        this.usedPoints = usedPoints;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserPointDTO convertToDTO(UserPoint entity) {
        if (entity == null) {
            return null;
        }

        UserPointDTO dto = new UserPointDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setTotalPoints(entity.getTotalPoints());
        dto.setAvailablePoints(entity.getAvailablePoints());
        dto.setUsedPoints(entity.getUsedPoints());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
    public static UserPoint convertToEntity(UserPointDTO dto) {
        if (dto == null) {
            return null;
        }

        UserPoint entity = new UserPoint();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setTotalPoints(dto.getTotalPoints());
        entity.setAvailablePoints(dto.getAvailablePoints());
        entity.setUsedPoints(dto.getUsedPoints());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
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
        return "UserPointDTO{" +
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