package com.project.codinviec_payment_service.service.imp.payment;

import com.project.codinviec_payment_service.dto.payment.PaymentMethodDTO;
import com.project.codinviec_payment_service.entity.PaymentMethod;
import com.project.codinviec_payment_service.exception.common.NotFoundIdExceptionHandler;
import com.project.codinviec_payment_service.mapper.payment.PaymentMethodMapper;
import com.project.codinviec_payment_service.repository.payment.PaymentMethodRepository;
import com.project.codinviec_payment_service.request.PageRequestCustom;
import com.project.codinviec_payment_service.request.payment.PaymentMethodRequest;
import com.project.codinviec_payment_service.service.payment.PaymentMethodService;
import com.project.codinviec_payment_service.specification.payment.PaymentMethodSpecification;
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
public class PaymentMethodServiceImp implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;
    private final PaymentMethodSpecification paymentMethodSpecification;
    private final PageCustomHelper pageCustomHelper;

    @Override
    public List<PaymentMethodDTO> getAll() {
        return paymentMethodMapper.paymentMethodDTOList(paymentMethodRepository.findAll());
    }

    @Override
    @Transactional
    public Page<PaymentMethodDTO> getAllWithPage(PageRequestCustom req) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(req);

        Specification<PaymentMethod> spec = paymentMethodSpecification.searchByName(pageRequestValidate.getKeyword());

        Sort sort = switch (pageRequestValidate.getSortBy()) {
            case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name");
            case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };

        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize(), sort);

        return paymentMethodRepository.findAll(spec, pageable)
                .map(paymentMethodMapper::paymentMethodDTO);
    }

    @Override
    public PaymentMethodDTO getById(Integer id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id"));

        return paymentMethodMapper.paymentMethodDTO(paymentMethod);
    }

    @Override
    @Transactional
    public PaymentMethodDTO create(PaymentMethodRequest req) {
        PaymentMethod paymentMethod = paymentMethodMapper.savePaymenMethodMapper(req);
        return paymentMethodMapper.paymentMethodDTO(paymentMethodRepository.save(paymentMethod));
    }

    @Override
    @Transactional
    public PaymentMethodDTO update(Integer id, PaymentMethodRequest req) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id"));

        paymentMethodMapper.updatePaymenMethodMapper(paymentMethod, req);
        return paymentMethodMapper.paymentMethodDTO(paymentMethodRepository.save(paymentMethod));
    }

    @Override
    @Transactional
    public PaymentMethodDTO deleteById(Integer id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy id"));
        paymentMethodRepository.deleteById(id);
        return paymentMethodMapper.paymentMethodDTO(paymentMethod);
    }
}
