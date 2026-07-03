package com.project.codinviec_payment_service.request.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Payment Method ID không được null")
    @Min(value = 1, message = "Payment Method ID phải lớn hơn 0")
    private Integer paymentMethodId;

    @NotNull(message = "Status ID không được null")
    @Min(value = 1, message = "Status ID phải lớn hơn 0")
    private Integer statusId;

    @NotNull(message = "Service Product ID không được null")
    @Min(value = 1, message = "Service Product ID phải lớn hơn 0")
    private Integer serviceProductId;

    @NotBlank(message = "User ID không được để trống")
    private String userId;
}
