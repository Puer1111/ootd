package com.ootd.ootd.model.dto.order;


import com.ootd.ootd.model.entity.order.Order;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@Data
public class OrderDTO {
    private Long orderId;
    private Long quantity;
    private LocalDateTime orderDate;
    private String merchantUid;

    private String username;

    private String productName;
    private Integer productPrice;
    private Integer salePercent;
    private Long totalPrice;

    private String orderStatus;

    public OrderDTO(Long orderId, Long quantity, LocalDateTime orderDate, String merchantUid, String username, String productName, Integer productPrice, Integer salePercent, Long totalPrice, String orderStatus) {
        this.orderId = orderId;
        this.quantity = quantity;
        this.orderDate = orderDate;
        this.merchantUid = merchantUid;
        this.username = username;
        this.productName = productName;
        this.productPrice = productPrice;
        this.salePercent = salePercent;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
    }

//    public OrderDTO(Long orderId, Long quantity, String orderDate, String merchantUid, String username, String productName, Integer productPrice, Integer salePercent, Long totalPrice, String orderStatus) {
//        this.orderId = orderId;
//        this.quantity = quantity;
//        this.orderDate = orderDate;
//        this.merchantUid = merchantUid;
//        this.username = username;
//        this.productName = productName;
//        this.productPrice = productPrice;
//        this.salePercent = salePercent;
//        this.totalPrice = totalPrice;
//        this.orderStatus = orderStatus;
//    }

    public static OrderDTO convertToDTO(Order order){
        return OrderDTO.builder().orderId(order.getOrderId()).quantity(order.getQuantity()).orderDate(order.getOrderDate())
                .merchantUid(order.getMerchantUid()).username(order.getUserName()).productName(order.getProductName())
                .productPrice(order.getProductPrice()).totalPrice(order.getTotalPrice())
                .salePercent(order.getSalePercent()).orderStatus(order.getOrderStatus()).build();
    }

    public static Order convertToEntity(OrderDTO dto){
        return Order.builder().quantity(dto.getQuantity()).orderDate(dto.getOrderDate()).merchantUid(dto.getMerchantUid())
                .userName(dto.getUsername()).productName(dto.getProductName()).productPrice(dto.getProductPrice()).salePercent(dto.getSalePercent())
                .totalPrice(dto.getTotalPrice()).orderStatus(dto.getOrderStatus()).build();
    }
}
