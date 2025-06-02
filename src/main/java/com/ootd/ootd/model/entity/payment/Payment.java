package com.ootd.ootd.model.entity.payment;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Payment {

    @Id
    @Column(name="payment_Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(name="imp_uid" ,nullable = false)
    private String impUid;
    @Column(name="product_No")
    private Long productNo;
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
    @Column(name="order_Date")
    private String orderDate;

    public Payment(Long paymentId, String impUid, Long productNo, String productName, String payMethod, String merchantUid, Integer totalPrice, String phone, String email, String userName, String orderDate) {
        this.paymentId = paymentId;
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
}
