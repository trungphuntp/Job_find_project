package com.project.codinviec_payment_service.mapper.payment;

import com.project.codinviec_payment_service.dto.payment.ServiceProductDTO;
import com.project.codinviec_payment_service.entity.ServiceProduct;
import com.project.codinviec_payment_service.request.payment.ServiceProductRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ServiceProductMapper {

    public ServiceProductDTO serviceProductDTO(ServiceProduct serviceProduct) {
        return ServiceProductDTO.builder()
                .id(serviceProduct.getId())
                .name(serviceProduct.getName())
                .description(serviceProduct.getDescription())
                .price(serviceProduct.getPrice())
                .images(serviceProduct.getImages())
                .userId(serviceProduct.getUserId())
                .jobId(serviceProduct.getJobId() != null ? serviceProduct.getJobId() : null)
                .createdDate(serviceProduct.getCreatedDate())
                .updatedDate(serviceProduct.getUpdatedDate())
                .build();
    }

    public ServiceProduct saveServiceProduct(ServiceProductRequest serviceProductRequest) {
        return ServiceProduct.builder()
                .name(serviceProductRequest.getName())
                .description(serviceProductRequest.getDescription())
                .price(serviceProductRequest.getPrice())
                .images(serviceProductRequest.getImages())
                .userId(serviceProductRequest.getUserId())
                .jobId(serviceProductRequest.getJobId())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    public void updateServiceProduct(ServiceProduct serviceProduct, ServiceProductRequest serviceProductRequest) {
        if (serviceProductRequest.getName() != null) {
            serviceProduct.setName(serviceProductRequest.getName());
        }

        if (serviceProductRequest.getDescription() != null) {
            serviceProduct.setDescription(serviceProductRequest.getDescription());
        }

        if (serviceProductRequest.getPrice() != 0) {
            serviceProduct.setPrice(serviceProductRequest.getPrice());
        }

        if (serviceProductRequest.getImages() != null) {
            serviceProduct.setImages(serviceProductRequest.getImages());
        }

        if (serviceProductRequest.getUserId() != null) {
            serviceProduct.setUserId(serviceProductRequest.getUserId());
        }

        if (serviceProductRequest.getJobId() != 0) {
            serviceProduct.setJobId(serviceProductRequest.getJobId());
        }

        serviceProduct.setCreatedDate(serviceProduct.getCreatedDate());
        serviceProduct.setUpdatedDate(LocalDateTime.now());
    }

    public List<ServiceProductDTO> serviceProductDTOList(List<ServiceProduct> serviceProductList) {
        return serviceProductList.stream().map(this::serviceProductDTO).toList();
    }
}
