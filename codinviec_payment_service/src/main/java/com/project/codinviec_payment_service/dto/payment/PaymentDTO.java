package com.project.codinviec_payment_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDTO {
    private Integer id;
    private String title;
    private String description;
    private String paymentMethod;
    private String status;
    private String serviceProduct;
    private String userId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
