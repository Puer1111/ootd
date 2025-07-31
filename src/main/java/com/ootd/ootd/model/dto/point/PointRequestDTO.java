package com.ootd.ootd.model.dto.point;

import com.ootd.ootd.model.entity.point.PointHistory;

public class PointRequestDTO {

    private Long points;
    private String description;
    private Long orderId;
    private PointHistory.PointType pointType;

    public PointRequestDTO() {}

    public PointRequestDTO(Long points, String description) {
        this.points = points;
        this.description = description;
    }

    public PointRequestDTO(Long points, String description, Long orderId) {
        this.points = points;
        this.description = description;
        this.orderId = orderId;
    }

    public PointRequestDTO(Long points, String description, Long orderId, PointHistory.PointType pointType) {
        this.points = points;
        this.description = description;
        this.orderId = orderId;
        this.pointType = pointType;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
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

    public PointHistory.PointType getPointType() {
        return pointType;
    }

    public void setPointType(PointHistory.PointType pointType) {
        this.pointType = pointType;
    }

    @Override
    public String toString() {
        return "PointRequestDTO{" +
                "points=" + points +
                ", description='" + description + '\'' +
                ", orderId=" + orderId +
                ", pointType=" + pointType +
                '}';
    }
}
