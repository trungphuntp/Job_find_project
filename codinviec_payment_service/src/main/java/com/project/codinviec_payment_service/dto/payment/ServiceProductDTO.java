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
public class ServiceProductDTO {
    private int id;
    private String name;
    private String description;
    private double price;
    private String images;
    private String userId;
    private int jobId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
