package com.ootd.ootd.model.dto.payment;

import com.ootd.ootd.model.entity.payment.Payment;
import lombok.*;

import java.util.Date;
@Getter
@NoArgsConstructor
@ToString
@Builder
@Data
public class PaymentDTO {
    private String impUid;
    private Long orderId;
    private String productName;
    private String payMethod;
    private String merchantUid;
    private Integer totalPrice;
    private String phone;
    private String email;
    private String userName;
    private String orderDate;
    private String paymentStatus;

    public PaymentDTO(String impUid, Long orderId, String productName, String payMethod, String merchantUid, Integer totalPrice, String phone, String email, String userName, String orderDate, String paymentStatus) {
        this.impUid = impUid;
        this.orderId = orderId;
        this.productName = productName;
        this.payMethod = payMethod;
        this.merchantUid = merchantUid;
        this.totalPrice = totalPrice;
        this.phone = phone;
        this.email = email;
        this.userName = userName;
        this.orderDate = orderDate;
        this.paymentStatus = paymentStatus;
    }

    public static Payment convertToEntity(PaymentDTO dto) {
        return Payment.builder().impUid(dto.getImpUid())
                .orderId(dto.getOrderId())
                .productName(dto.getProductName())
                .payMethod(dto.getPayMethod())
                .merchantUid(dto.getMerchantUid())
                .totalPrice(dto.getTotalPrice())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .userName(dto.getUserName())
                .orderDate(dto.getOrderDate())
                .paymentStatus(dto.getPaymentStatus())
                .build();
    }
    public static PaymentDTO convertToDTO(Payment entity) {
        return PaymentDTO.builder()
                .impUid(entity.getImpUid())
                .orderId(entity.getOrderId())
                .productName(entity.getProductName())
                .payMethod(entity.getPayMethod())
                .merchantUid(entity.getMerchantUid())
                .totalPrice(entity.getTotalPrice())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .userName(entity.getUserName())
                .orderDate(entity.getOrderDate())
                .paymentStatus(entity.getPaymentStatus())
                .build();
    }
}
