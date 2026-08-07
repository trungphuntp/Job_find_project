package com.project.codinviec_core_service.service.imp;

import com.project.codinviec_core_service.dto.CompanySizeDTO;
import com.project.codinviec_core_service.entity.CompanySize;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.mapper.CompanySizeMapper;
import com.project.codinviec_core_service.repository.CompanySizeRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.SaveUpdateCompanySizeRequest;
import com.project.codinviec_core_service.service.CompanySizeService;
import com.project.codinviec_core_service.specification.CompanySizeSpecification;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanySizeServiceImp implements CompanySizeService {
    private final CompanySizeRepository companySizeRepository;
    private final CompanySizeMapper companySizeMapper;
    private final PageCustomHelper pageCustomHelper;
    private final CompanySizeSpecification companySizeSpecification;

    @Override
    public List<CompanySizeDTO> getAllCompany() {
        return companySizeRepository.findAll().stream().map(companySizeMapper::companySizeToCompanySizeDTO).toList();
    }

    @Override
    public Page<CompanySizeDTO> getAllCompanyPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(pageRequestCustom);

        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize());

        Specification<CompanySize> spec = Specification.allOf(
                companySizeSpecification.searchByName(pageRequestCustom.getKeyword()));

        return companySizeRepository.findAll(spec, pageable).map(
                companySizeMapper::companySizeToCompanySizeDTO);
    }

    @Override
    public CompanySizeDTO getCompanyById(Integer id) {
        CompanySize companySize = companySizeRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company size"));
        return companySizeMapper.companySizeToCompanySizeDTO(companySize);
    }

    @Override
    @Transactional
    public CompanySizeDTO saveCompanySize(SaveUpdateCompanySizeRequest saveUpdateCompanySizeRequest) {
        try {
            CompanySize companySize = companySizeMapper.saveCompanySizeMapper(saveUpdateCompanySizeRequest);
            return companySizeMapper.companySizeToCompanySizeDTO(companySizeRepository.save(companySize));
        } catch (Exception e) {
            throw new AppException(ResourceErrorCode.CONFLICT, "Lỗi thêm company size!");
        }
    }

    @Override
    @Transactional
    public CompanySizeDTO updateCompanySize(Integer idCompanySize, SaveUpdateCompanySizeRequest saveUpdateCompanySizeRequest) {
        companySizeRepository.findById(idCompanySize)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company size!"));
        try {
            CompanySize mappedCompanySize = companySizeMapper.updateCompanySizeMapper(idCompanySize, saveUpdateCompanySizeRequest);
            return companySizeMapper.companySizeToCompanySizeDTO(companySizeRepository.save(mappedCompanySize));
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ResourceErrorCode.CONFLICT, "Lỗi cập nhật company size!");
        }
    }

    @Override
    @Transactional
    public CompanySizeDTO deleteCompanySize(Integer id) {
        CompanySize companySize = companySizeRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company size"));
        companySizeRepository.delete(companySize);
        return companySizeMapper.companySizeToCompanySizeDTO(companySize);
    }
}
