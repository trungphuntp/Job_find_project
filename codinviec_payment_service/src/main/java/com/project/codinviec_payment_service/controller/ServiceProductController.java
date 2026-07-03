package com.project.codinviec_payment_service.controller;

import com.project.codinviec_payment_service.request.PageRequestCustom;
import com.project.codinviec_payment_service.request.payment.ServiceProductRequest;
import com.project.codinviec_payment_service.response.BaseResponse;
import com.project.codinviec_payment_service.service.payment.ServiceProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-product")
@RequiredArgsConstructor
public class ServiceProductController {

    private final ServiceProductService serviceProductService;

    @GetMapping
    public ResponseEntity<?> getAll(PageRequestCustom pageRequestCustom) {
        if (pageRequestCustom.getPageNumber() == 0
                && pageRequestCustom.getPageSize() == 0
                && pageRequestCustom.getKeyword() == null
                && pageRequestCustom.getSortBy() == null) {
            return ResponseEntity.ok(BaseResponse.success(serviceProductService.getAll(), "OK"));
        }

        return ResponseEntity.ok(BaseResponse.success(serviceProductService.getAllWithPage(pageRequestCustom), "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(BaseResponse.success(serviceProductService.getById(id), "OK"));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ServiceProductRequest request) {
        return ResponseEntity.ok(BaseResponse.success(serviceProductService.create(request), "OK"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody ServiceProductRequest request) {
        return ResponseEntity.ok(BaseResponse.success(serviceProductService.update(id, request), "OK"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        serviceProductService.deleteById(id);
        return ResponseEntity.ok(BaseResponse.success(null, "OK"));
    }
}
