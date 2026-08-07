package com.project.codinviec_core_service.service.imp;

import com.project.codinviec_core_service.dto.CVUserDTO;
import com.project.codinviec_core_service.entity.CVUser;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.mapper.CVUserMapper;
import com.project.codinviec_core_service.repository.CVUserRepository;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.request.CVUserRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.CVUserService;
import com.project.codinviec_core_service.service.file.FileService;
import com.project.codinviec_core_service.specification.CVUserSpecification;
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
public class CVUserServiceImp implements CVUserService {

    private final CVUserRepository cvUserRepository;
    private final UserRepository userRepository;
    private final CVUserMapper cvUserMapper;
    private final PageCustomHelper pageCustomHelper;
    private final CVUserSpecification cvUserSpecification;
    private final FileService fileService;

    @Override
    public List<CVUserDTO> getAll() {
        return cvUserMapper.cvUserDTOListToCVUserDTOList(cvUserRepository.findAll());
    }

    @Override
    @Transactional
    public Page<CVUserDTO> getAllWithPage(PageRequestCustom req) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(req);

        Specification<CVUser> spec = cvUserSpecification.searchByTitle(pageRequestValidate.getKeyword());

        Sort sort = switch (pageRequestValidate.getSortBy()) {
            case "versionAsc" -> Sort.by(Sort.Direction.ASC, "version");
            case "versionDesc" -> Sort.by(Sort.Direction.DESC, "version");
            case "titleAsc" -> Sort.by(Sort.Direction.ASC, "title");
            case "titleDesc" -> Sort.by(Sort.Direction.DESC, "title");
            case "isActiveAsc" -> Sort.by(Sort.Direction.ASC, "isActive");
            case "isActiveDesc" -> Sort.by(Sort.Direction.DESC, "isActive");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };

        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize(), sort);

        return cvUserRepository.findAll(spec, pageable)
                .map(cvUserMapper::toCVUserDTO);
    }

    @Override
    public CVUserDTO getById(Integer id) {
        CVUser cvUser = cvUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id CV User"));
        return cvUserMapper.toCVUserDTO(cvUser);
    }

    @Override
    @Transactional
    public CVUserDTO create(CVUserRequest req) {
        User candidate = userRepository.findById(req.getCandidateId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id User"));

        Integer maxVerison = cvUserRepository.findFirstByCandidate_IdOrderByVersionDesc(req.getCandidateId())
                .map(cv -> cv.getVersion() + 1)
                .orElse(1);

        if (req.getFileUrl() == null || req.getFileUrl().isEmpty()) {
            throw new AppException(ResourceErrorCode.NOT_FOUND, "Vui lòng cung cấp file hình ảnh CV!");
        }

        String savedFileName = fileService.saveFiles(req.getFileUrl());

        CVUser cvUser = cvUserMapper.toCreateCVUser(req, candidate, savedFileName);
        cvUser.setVersion(maxVerison);

        return cvUserMapper.toCVUserDTO(cvUserRepository.save(cvUser));
    }

    @Override
    @Transactional
    public CVUserDTO update(Integer id, CVUserRequest req) {
        CVUser cvUser = cvUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id CV User"));

        if (!cvUser.getCandidate().getId().equals(req.getCandidateId())) {
            throw new AppException(ResourceErrorCode.NOT_FOUND, "CV không thuộc user này!");
        }

        String fileUrl = cvUser.getFileUrl();
        if (req.getFileUrl() != null && !req.getFileUrl().isEmpty()) {
            fileUrl = fileService.saveFiles(req.getFileUrl());
        }

        CVUser mappedCVUser = cvUserMapper.toUpdateCVUser(cvUser, req, fileUrl);
        mappedCVUser.setCreatedAt(cvUser.getCreatedAt());
        return cvUserMapper.toCVUserDTO(cvUserRepository.save(mappedCVUser));
    }

    @Override
    @Transactional
    public CVUserDTO deleteById(Integer id, String candidateId) {
        CVUser cvUser = cvUserRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id CV User"));

        if (!cvUser.getCandidate().getId().equals(candidateId)) {
            throw new AppException(ResourceErrorCode.UNAUTHORIZED_ACTION, "Bạn không có quyền xóa CV này!");
        }

        cvUserRepository.delete(cvUser);
        return cvUserMapper.toCVUserDTO(cvUser);
    }
}
