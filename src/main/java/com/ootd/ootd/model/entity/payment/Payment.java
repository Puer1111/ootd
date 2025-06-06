package com.ootd.ootd.model.entity.payment;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Entity
@NoArgsConstructor
@Builder
@Setter
public class Payment {

    @Id
    @Column(name="payment_Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(name="imp_uid" ,nullable = false)
    private String impUid;
    @Column(name="orders_Id")
    private Long orderId;
    @Column(name="product_Name" ,nullable = false)
    private String productName;
    @Column(name="pay_Method")
    private String payMethod;
    @Column(name="merchant_uid" ,nullable = false)
    private String merchantUid;
    @Column(name="totalPrice" ,nullable = false)
    private Integer totalPrice;
    @Column(name="user_Phone")
    private String phone;
    @Column(name="user_Email")
    private String email;
    @Column(name="user_Name")
    private String userName;
    @Column(name="orders_Date")
    private String orderDate;

    @Column(name="payment_status")
    private String paymentStatus = "success";

    public Payment(Long paymentId, String impUid, Long orderId, String productName, String payMethod, String merchantUid, Integer totalPrice, String phone, String email, String userName, String orderDate, String paymentStatus) {
        this.paymentId = paymentId;
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
        this.paymentStatus = "success";
    }
}
