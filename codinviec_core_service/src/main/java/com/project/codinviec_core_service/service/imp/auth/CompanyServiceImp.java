package com.project.codinviec_core_service.service.imp.auth;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.auth.CompanyDTO;
import com.project.codinviec_core_service.entity.CompanyAddress;
import com.project.codinviec_core_service.entity.CompanySize;
import com.project.codinviec_core_service.entity.StatusSpecialCompany;
import com.project.codinviec_core_service.entity.auth.Company;
import com.project.codinviec_core_service.mapper.CompanyAddressMapper;
import com.project.codinviec_core_service.mapper.CompanySizeMapper;
import com.project.codinviec_core_service.mapper.StatusSpecialMapper;
import com.project.codinviec_core_service.mapper.auth.CompanyMapper;
import com.project.codinviec_core_service.repository.CompanyAddressRepository;
import com.project.codinviec_core_service.repository.CompanySizeRepository;
import com.project.codinviec_core_service.repository.JobRepository;
import com.project.codinviec_core_service.repository.StatusSpecialCompanyRepository;
import com.project.codinviec_core_service.repository.auth.CompanyRepository;
import com.project.codinviec_core_service.request.GetCompanyFeaturedRequest;
import com.project.codinviec_core_service.request.PageRequestCompany;
import com.project.codinviec_core_service.request.auth.SaveUpdateCompanyRequest;
import com.project.codinviec_core_service.service.auth.CompanyService;
import com.project.codinviec_core_service.specification.auth.CompanySpecification;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImp implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final PageCustomHelper PageCustomHelper;
    private final CompanySpecification companySpecification;
    private final CompanySizeRepository companySizeRepository;
    private final CompanySizeMapper companySizeMapper;
    private final CompanyAddressRepository companyAddressRepository;
    private final CompanyAddressMapper companyAddressMapper;
    private final JobRepository jobRepository;
    private final StatusSpecialCompanyRepository statusSpecialCompanyRepository;
    private final StatusSpecialMapper statusSpecialMapper;

    private void enrichCompanyDTOs(List<CompanyDTO> companyDTOs) {
        List<String> ids = companyDTOs.stream().map(CompanyDTO::getId).toList();
        Map<String, List<CompanyAddress>> mapCompanyAddress = companyAddressRepository
                .findByCompanyIdsWithLocation(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getCompany().getId()));
        Map<String, List<StatusSpecialCompany>> mapStatusSpecial = statusSpecialCompanyRepository
                .findByCompanyIdsWithStatus(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getIdCompany().getId()));
        Map<String, Long> mapJobCount = jobRepository.countJobByCompanyIds(ids).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        for (CompanyDTO companyDTO : companyDTOs) {
            companyDTO.setCompanyAddress(
                    mapCompanyAddress.getOrDefault(companyDTO.getId(), List.of()).stream()
                            .map(companyAddressMapper::toCompanyAddressDTO).toList()
            );
            companyDTO.setStatusSpecials(
                    statusSpecialMapper.StatusSpecialCompanyToStatusSpecialDTO(
                            mapStatusSpecial.getOrDefault(companyDTO.getId(), List.of()))
            );
            companyDTO.setJobActive(mapJobCount.getOrDefault(companyDTO.getId(), 0L).intValue());
        }
    }

    @Override
    public List<CompanyDTO> getAllCompany() {
        List<CompanyDTO> listCompanyDTO = companyRepository.findAll().stream()
                .map(companyMapper::companyToCompanyDTO).toList();
        enrichCompanyDTOs(listCompanyDTO);
        return listCompanyDTO;
    }

    @Override
    public List<CompanyDTO> getCompanyFeatured(GetCompanyFeaturedRequest getCompanyFeaturedRequest) {
        int limit = getCompanyFeaturedRequest.getLimit() > 0 ? getCompanyFeaturedRequest.getLimit() : 8;
        Pageable pageable = PageRequest.of(0, limit);
        List<CompanyDTO> listCompanyDTO = companyRepository.findByIsFeaturedTrue(pageable).stream()
                .map(companyMapper::companyToCompanyDTO).toList();
        enrichCompanyDTOs(listCompanyDTO);
        return listCompanyDTO;
    }

    @Override
    public Page<CompanyDTO> getAllCompanyPage(PageRequestCompany pageRequestCompany) {
        PageRequestCompany pageRequestValidate = PageCustomHelper.validatePageCompany(pageRequestCompany);
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1,
                pageRequestValidate.getPageSize());
        Specification<Company> spec = Specification.allOf(
                companySpecification.fetchDetails(),
                companySpecification.searchByName(pageRequestValidate.getKeyword()),
                companySpecification.minEmployees(pageRequestValidate.getMinEmployees()),
                companySpecification.maxEmployees(pageRequestValidate.getMaxEmployees()),
                companySpecification.hasProvince(pageRequestValidate.getLocation()));
        Page<CompanyDTO> companyDTOPage = companyRepository.findAll(spec, pageable)
                .map(companyMapper::companyToCompanyDTO);
        enrichCompanyDTOs(companyDTOPage.getContent());
        return companyDTOPage;
    }

    @Override
    public CompanyDTO getCompanyById(String idCompany) {
        Company company = companyRepository.findById(idCompany)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company!"));
        CompanyDTO companyDTO = companyMapper.companyToCompanyDTO(company);
        companyDTO.setStatusSpecials(statusSpecialMapper
                .StatusSpecialCompanyToStatusSpecialDTO(statusSpecialCompanyRepository
                        .findByIdCompany_Id(companyDTO.getId())));
        companyDTO.setCompanySize(companySizeMapper.companySizeToCompanySizeDTO(companySizeRepository
                .findByCompanies_Id(companyDTO.getId()).orElse(null)));
        companyDTO.setCompanyAddress(
                companyAddressRepository.findByCompany_Id(companyDTO.getId())
                        .stream().map(companyAddressMapper::toCompanyAddressDTO).toList()
        );
        return companyDTO;
    }

    @Override
    @Transactional
    public CompanyDTO saveCompany(SaveUpdateCompanyRequest saveUpdateCompanyRequest) {
        CompanySize companySize = companySizeRepository.findById(saveUpdateCompanyRequest.getCompanySizeId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company size!"));
        Company company = companyMapper.saveCompanyMapper(companySize, saveUpdateCompanyRequest);
        return companyMapper.companyToCompanyDTO(companyRepository.save(company));
    }

    @Override
    @Transactional
    public CompanyDTO updateCompany(String idCompany, SaveUpdateCompanyRequest saveUpdateCompanyRequest) {
        CompanySize companySize = companySizeRepository.findById(saveUpdateCompanyRequest.getCompanySizeId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company size!"));
        Company company = companyRepository.findById(idCompany)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company!"));
        Company mappedCompany = companyMapper.updateCompanyMapper(idCompany, companySize, saveUpdateCompanyRequest);
        mappedCompany.setCreatedDate(company.getCreatedDate());
        return companyMapper.companyToCompanyDTO(companyRepository.save(mappedCompany));
    }

    @Override
    @Transactional
    public CompanyDTO deleteCompany(String idCompany) {
        Company company = companyRepository.findById(idCompany)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company!"));
        companyRepository.delete(company);
        return companyMapper.companyToCompanyDTO(company);
    }
}
