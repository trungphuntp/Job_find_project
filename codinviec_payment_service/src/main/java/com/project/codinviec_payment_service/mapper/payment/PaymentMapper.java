package com.project.codinviec_payment_service.mapper.payment;

import com.project.codinviec_payment_service.dto.payment.PaymentDTO;
import com.project.codinviec_payment_service.entity.Payment;
import com.project.codinviec_payment_service.entity.PaymentMethod;
import com.project.codinviec_payment_service.entity.PaymentStatus;
import com.project.codinviec_payment_service.entity.ServiceProduct;
import com.project.codinviec_payment_service.request.payment.PaymentRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentMapper {

    public PaymentDTO paymentDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .title(payment.getTitle())
                .description(payment.getDescription())
                .paymentMethod(payment.getPaymentMethod().getName())
                .status(payment.getPaymentStatus().getName())
                .serviceProduct(payment.getServiceProduct().getName())
                .userId(payment.getUserId())
                .createdDate(payment.getCreatedDate())
                .updatedDate(payment.getUpdatedDate())
                .build();
    }

    public Payment savePayment(PaymentRequest paymentRequest) {
        return Payment.builder()
                .title(paymentRequest.getTitle())
                .description(paymentRequest.getDescription())
                .paymentMethod(PaymentMethod.builder()
                        .id(paymentRequest.getPaymentMethodId())
                        .build())
                .paymentStatus(PaymentStatus.builder()
                        .id(paymentRequest.getStatusId())
                        .build())
                .serviceProduct(ServiceProduct.builder()
                        .id(paymentRequest.getServiceProductId())
                        .build())
                .userId(paymentRequest.getUserId())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    public void updatePayment(Payment payment, PaymentRequest req) {
        if (payment == null || req == null) return;

        if (req.getTitle() != null) {
            payment.setTitle(req.getTitle());
        }

        if (req.getDescription() != null) {
            payment.setDescription(req.getDescription());
        }

        if (req.getPaymentMethodId() != null && req.getPaymentMethodId() != 0) {
            payment.setPaymentMethod(PaymentMethod.builder()
                    .id(req.getPaymentMethodId())
                    .build());
        }

        if (req.getStatusId() != null && req.getStatusId() != 0) {
            payment.setPaymentStatus(PaymentStatus.builder()
                    .id(req.getStatusId())
                    .build());
        }

        if (req.getServiceProductId() != null && req.getServiceProductId() != 0) {
            payment.setServiceProduct(ServiceProduct.builder()
                    .id(req.getServiceProductId())
                    .build());
        }

        if (req.getUserId() != null) {
            payment.setUserId(req.getUserId());
        }

        payment.setCreatedDate(payment.getCreatedDate());
        payment.setUpdatedDate(LocalDateTime.now());
    }

    public List<PaymentDTO> paymentDTOList(List<Payment> paymentList) {
        return paymentList.stream().map(this::paymentDTO).toList();
    }
}
