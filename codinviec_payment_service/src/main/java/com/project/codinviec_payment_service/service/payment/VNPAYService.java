package com.project.codinviec_payment_service.service.payment;

import com.project.codinviec_payment_service.dto.vnpay.VNPAYCallBackResponseDTO;
import com.project.codinviec_payment_service.dto.vnpay.VNPAYPaymentResponseDTO;
import com.project.codinviec_payment_service.request.payment.PaymentRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface VNPAYService {
    VNPAYPaymentResponseDTO createPaymentUrl(PaymentRequest paymentRequest, HttpServletRequest request);
    VNPAYCallBackResponseDTO handleIpn(HttpServletRequest request);
}
