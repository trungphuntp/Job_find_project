package com.project.codinviec_core_service.service.imp.payment;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.payment.PaymentDTO;
import com.project.codinviec_core_service.entity.payment.Payment;
import com.project.codinviec_core_service.mapper.payment.PaymentMapper;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.repository.payment.PaymentMethodRepository;
import com.project.codinviec_core_service.repository.payment.PaymentRepository;
import com.project.codinviec_core_service.repository.payment.PaymentStatusRepository;
import com.project.codinviec_core_service.repository.payment.ServiceProductRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.payment.PaymentRequest;
import com.project.codinviec_core_service.service.payment.PaymentService;
import com.project.codinviec_core_service.specification.payment.PaymentSpecification;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
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
    private final UserRepository userRepository;

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
            case "userAsc" -> Sort.by(Sort.Direction.ASC, "user");
            case "userDesc" -> Sort.by(Sort.Direction.DESC, "user");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };

        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1,  pageRequestValidate.getPageSize(), sort);

        return paymentRepository.findAll(spec,pageable)
                .map(paymentMapper::paymentDTO);
    }

    @Override
    public PaymentDTO getById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Id payment"));

        return paymentMapper.paymentDTO(payment);
    }



    @Override
    @Transactional
    public PaymentDTO create(PaymentRequest req) {

        paymentMethodRepository.findById(req.getPaymentMethodId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id PaymentMethod"));

        paymentStatusRepository.findById(req.getStatusId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id PaymentStatus"));

        serviceProductRepository.findById(req.getServiceProductId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id ServiceProduct"));

        // Thay thế jwt sau này
        userRepository.findById(req.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id User"));

        Payment payment = paymentMapper.savePayment(req);
        return paymentMapper.paymentDTO(paymentRepository.save(payment));

    }

    @Override
    @Transactional
    public PaymentDTO update(Integer id, PaymentRequest req) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Id Payment"));

        if (req.getPaymentMethodId() != null) {
            paymentMethodRepository.findById(req.getPaymentMethodId())
                    .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id PaymentMethod"));
        }

        if (req.getStatusId() != null) {
            paymentStatusRepository.findById(req.getStatusId())
                    .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id PaymentStatus"));
        }

        if (req.getServiceProductId() != null) {
            serviceProductRepository.findById(req.getServiceProductId())
                    .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id ServiceProduct"));
        }

        if (req.getUserId() != null) {
            userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id User"));
        }


        paymentMapper.updatePayment(payment,req);
        return paymentMapper.paymentDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentDTO deleteById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Id Payment"));
        paymentRepository.delete(payment);
        return paymentMapper.paymentDTO(payment);
    }
}
