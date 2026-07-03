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
public class ServiceProductRequest {
    @NotBlank(message = "Tên sản phẩm dịch vụ không được để trống")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Giá không được null")
    @Min(value = 0, message = "Giá phải lớn hơn hoặc bằng 0")
    private double price;

    private String images;

    @NotBlank(message = "User ID không được để trống")
    private String userId;

    @NotNull(message = "Job ID không được null")
    @Min(value = 1, message = "Job ID phải lớn hơn 0")
    private int jobId;
}
