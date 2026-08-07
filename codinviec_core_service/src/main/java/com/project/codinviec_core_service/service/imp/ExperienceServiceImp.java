package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.ExperienceDTO;
import com.project.codinviec_core_service.entity.Experience;
import com.project.codinviec_core_service.mapper.ExperienceMapper;
import com.project.codinviec_core_service.repository.ExperienceRepository;
import com.project.codinviec_core_service.request.ExperienceRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.ExperienceService;
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
public class ExperienceServiceImp implements ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ExperienceMapper experienceMapper;
    private final PageCustomHelper pageCustomHelper;

    @Override
    public List<ExperienceDTO> getAllExperience() {
        return experienceRepository.findAll()
                .stream()
                .map(experienceMapper::toDto)
                .toList();
    }

    @Override
    public Page<ExperienceDTO> getAllExperiencePage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom validated = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(validated.getPageNumber() - 1, validated.getPageSize());
        return experienceRepository.findAll(pageable).map(experienceMapper::toDto);
    }

    @Override
    public ExperienceDTO getExperienceById(Integer id) {
        Experience entity = experienceRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id experience"));
        return experienceMapper.toDto(entity);
    }

    @Override
    @Transactional
    public ExperienceDTO createExperience(ExperienceRequest request) {
        Experience entity = experienceMapper.saveExperience(request);
        experienceRepository.save(entity);
        return experienceMapper.toDto(entity);
    }

    @Override
    @Transactional
    public ExperienceDTO updateExperience(int id, ExperienceRequest request) {
        experienceRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id experience"));

        Experience entity = experienceMapper.updateExperience(id, request);
        Experience updated = experienceRepository.save(entity);
        return experienceMapper.toDto(updated);
    }

    @Override
    @Transactional
    public ExperienceDTO deleteExperience(int id) {
        Experience entity = experienceRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id experience"));
        experienceRepository.delete(entity);
        return experienceMapper.toDto(entity);
    }
}
