package com.ootd.ootd.service.payment;

import com.ootd.ootd.model.dto.payment.PaymentDTO;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

public interface PaymentService {

    IamportResponse<Payment> validatePayment(String impUid);
    void savePayment(PaymentDTO dto);

    String getImpUid(Long orderNo);

    IamportResponse<Payment> cancelPayment(String imp_uid);
}
