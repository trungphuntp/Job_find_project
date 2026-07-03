package com.project.codinviec_payment_service.service.imp.payment;

import com.project.codinviec_payment_service.dto.payment.PaymentDTO;
import com.project.codinviec_payment_service.entity.Payment;
import com.project.codinviec_payment_service.exception.common.NotFoundIdExceptionHandler;
import com.project.codinviec_payment_service.mapper.payment.PaymentMapper;
import com.project.codinviec_payment_service.repository.payment.PaymentMethodRepository;
import com.project.codinviec_payment_service.repository.payment.PaymentRepository;
import com.project.codinviec_payment_service.repository.payment.PaymentStatusRepository;
import com.project.codinviec_payment_service.repository.payment.ServiceProductRepository;
import com.project.codinviec_payment_service.request.PageRequestCustom;
import com.project.codinviec_payment_service.request.payment.PaymentRequest;
import com.project.codinviec_payment_service.service.payment.PaymentService;
import com.project.codinviec_payment_service.specification.payment.PaymentSpecification;
import com.project.codinviec_payment_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PageCustomHelper pageCustomHelper;
    private final PaymentSpecification paymentSpecification;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ServiceProductRepository serviceProductRepository;

    @Override
    public List<PaymentDTO> getAll() {
        return paymentMapper.paymentDTOList(paymentRepository.findAll());
    }

    @Override
    public Page<PaymentDTO> getAllWithPage(PageRequestCustom req) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(req);

        Specification<Payment> spec = paymentSpecification.searchByTitle(pageRequestValidate.getKeyword());

        Sort sort = switch (pageRequestValidate.getSortBy()) {
            case "titleAsc" -> Sort.by(Sort.Direction.ASC, "title");
            case "titleDesc" -> Sort.by(Sort.Direction.DESC, "title");
            case "descriptionAsc" -> Sort.by(Sort.Direction.ASC, "description");
            case "descriptionDesc" -> Sort.by(Sort.Direction.DESC, "description");
            case "paymentMethodAsc" -> Sort.by(Sort.Direction.ASC, "paymentMethod");
            case "paymentMethodDesc" -> Sort.by(Sort.Direction.DESC, "paymentMethod");
            case "paymentStatusAsc" -> Sort.by(Sort.Direction.ASC, "paymentStatus");
            case "paymentStatusDesc" -> Sort.by(Sort.Direction.DESC, "paymentStatus");
            case "serviceProductAsc" -> Sort.by(Sort.Direction.ASC, "serviceProduct");
            case "serviceProductDesc" -> Sort.by(Sort.Direction.DESC, "serviceProduct");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };

        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize(), sort);

        return paymentRepository.findAll(spec, pageable)
                .map(paymentMapper::paymentDTO);
    }

    @Override
    public PaymentDTO getById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy Id payment"));

        return paymentMapper.paymentDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO create(PaymentRequest req) {
        paymentMethodRepository.findById(req.getPaymentMethodId())
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id PaymentMethod"));

        paymentStatusRepository.findById(req.getStatusId())
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id PaymentStatus"));

        serviceProductRepository.findById(req.getServiceProductId())
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id ServiceProduct"));

        Payment payment = paymentMapper.savePayment(req);
        return paymentMapper.paymentDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentDTO update(Integer id, PaymentRequest req) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy Id Payment"));

        if (req.getPaymentMethodId() != null) {
            paymentMethodRepository.findById(req.getPaymentMethodId())
                    .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id PaymentMethod"));
        }

        if (req.getStatusId() != null) {
            paymentStatusRepository.findById(req.getStatusId())
                    .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id PaymentStatus"));
        }

        if (req.getServiceProductId() != null) {
            serviceProductRepository.findById(req.getServiceProductId())
                    .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id ServiceProduct"));
        }

        paymentMapper.updatePayment(payment, req);
        return paymentMapper.paymentDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentDTO deleteById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy Id Payment"));
        paymentRepository.delete(payment);
        return paymentMapper.paymentDTO(payment);
    }
}
