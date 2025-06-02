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
    private Long productNo;
    private String productName;
    private String payMethod;
    private String merchantUid;
    private Integer totalPrice;
    private String phone;
    private String email;
    private String userName;
    private String orderDate;

    public PaymentDTO(String impUid, Long productNo, String productName, String payMethod, String merchantUid, Integer totalPrice, String phone, String email, String userName, String orderDate) {
        this.impUid = impUid;
        this.productNo = productNo;
        this.productName = productName;
        this.payMethod = payMethod;
        this.merchantUid = merchantUid;
        this.totalPrice = totalPrice;
        this.phone = phone;
        this.email = email;
        this.userName = userName;
        this.orderDate = orderDate;
    }

    public static Payment convertToEntity(PaymentDTO dto) {
        return Payment.builder().impUid(dto.getImpUid())
                .productNo(dto.getProductNo())
                .productName(dto.getProductName())
                .payMethod(dto.getPayMethod())
                .merchantUid(dto.getMerchantUid())
                .totalPrice(dto.getTotalPrice())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .userName(dto.getUserName())
                .orderDate(dto.getOrderDate())
                .build();
    }
    public static PaymentDTO convertToDTO(Payment entity) {
        return PaymentDTO.builder()
                .impUid(entity.getImpUid())
                .productNo(entity.getProductNo())
                .productName(entity.getProductName())
                .payMethod(entity.getPayMethod())
                .merchantUid(entity.getMerchantUid())
                .totalPrice(entity.getTotalPrice())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .userName(entity.getUserName())
                .orderDate(entity.getOrderDate())
                .build();
    }
}
