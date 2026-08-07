package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.AwardDTO;
import com.project.codinviec_core_service.entity.Award;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.mapper.AwardMapper;
import com.project.codinviec_core_service.repository.AwardRepository;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.request.AwardRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.AwardService;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AwardServiceImp implements AwardService {

    private final AwardRepository awardRepository;
    private final UserRepository userRepository;
    private final AwardMapper awardMapper;
    private final PageCustomHelper pageCustomHelper;

    @Override
    public List<AwardDTO> getAllAward() {
        return awardRepository.findAll()
                .stream()
                .map(awardMapper::toDto)
                .toList();
    }

    @Override
    public Page<AwardDTO> getAllAwardPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom validated = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(validated.getPageNumber() - 1, validated.getPageSize());
        return awardRepository.findAll(pageable).map(awardMapper::toDto);
    }

    @Override
    public List<AwardDTO> getAwardByUser(String userId) {
        return awardRepository.findByUser_Id(userId)
                .stream()
                .map(awardMapper::toDto)
                .toList();
    }

    @Override
    public AwardDTO getAwardById(Integer id) {
        Award award = awardRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id award"));
        return awardMapper.toDto(award);
    }

    @Override
    @Transactional
    public AwardDTO createAward(AwardRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));

        Award award = awardMapper.saveAward(user, request);
        awardRepository.save(award);
        return awardMapper.toDto(award);
    }

    @Override
    @Transactional
    public AwardDTO updateAward(int id, AwardRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        awardRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id award"));

        Award award = awardMapper.updateAward(id, user, request);
        Award updated = awardRepository.save(award);
        return awardMapper.toDto(updated);
    }

    @Override
    @Transactional
    public AwardDTO deleteAward(int id) {
        Award award = awardRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id award"));
        awardRepository.delete(award);
        return awardMapper.toDto(award);
    }
}
