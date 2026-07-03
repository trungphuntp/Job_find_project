package com.project.codinviec_payment_service.mapper.payment;

import com.project.codinviec_payment_service.dto.payment.PaymentMethodDTO;
import com.project.codinviec_payment_service.entity.PaymentMethod;
import com.project.codinviec_payment_service.request.payment.PaymentMethodRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentMethodMapper {

    public PaymentMethodDTO paymentMethodDTO(PaymentMethod paymentMethod) {
        return PaymentMethodDTO.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .createdDate(paymentMethod.getCreatedDate())
                .build();
    }

    public PaymentMethod savePaymenMethodMapper(PaymentMethodRequest paymentMethodRequest) {
        if (paymentMethodRequest == null) return null;
        return PaymentMethod.builder()
                .name(paymentMethodRequest.getName())
                .createdDate(LocalDateTime.now())
                .build();
    }

    public void updatePaymenMethodMapper(PaymentMethod paymentMethod, PaymentMethodRequest paymentMethodRequest) {
        if (paymentMethodRequest.getName() != null) {
            paymentMethod.setName(paymentMethodRequest.getName());
        }
    }

    public List<PaymentMethodDTO> paymentMethodDTOList(List<PaymentMethod> paymentMethodsList) {
        return paymentMethodsList.stream().map(this::paymentMethodDTO).toList();
    }
}
