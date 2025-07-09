package com.ootd.ootd.model.dto.point;

import com.ootd.ootd.model.entity.point.PointHistory;

/**
 * 적립금 사용/적립 요청용 DTO
 * 경로: src/main/java/com/ootd/ootd/model/dto/point/PointRequestDTO.java
 */
public class PointRequestDTO {

    private Long points;                    // 사용/적립할 적립금
    private String description;             // 사용/적립 사유
    private Long orderId;                   // 관련 주문 ID
    private PointHistory.PointType pointType; // 적립금 타입

    // 기본 생성자
    public PointRequestDTO() {}

    // 적립금 사용용 생성자
    public PointRequestDTO(Long points, String description) {
        this.points = points;
        this.description = description;
    }

    // 주문 관련 생성자
    public PointRequestDTO(Long points, String description, Long orderId) {
        this.points = points;
        this.description = description;
        this.orderId = orderId;
    }

    // 전체 필드 생성자
    public PointRequestDTO(Long points, String description, Long orderId, PointHistory.PointType pointType) {
        this.points = points;
        this.description = description;
        this.orderId = orderId;
        this.pointType = pointType;
    }

    // Getter & Setter
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