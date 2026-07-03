package com.project.codinviec_payment_service.dto.vnpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VNPAYCallBackResponseDTO {
    private String code;
    private String message;
    private Integer paymentId;
}
