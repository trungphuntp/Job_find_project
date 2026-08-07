package com.project.codinviec_core_service.service.imp;

import com.project.codinviec_core_service.dto.LanguageSkillDTO;
import com.project.codinviec_core_service.entity.Language;
import com.project.codinviec_core_service.entity.LanguageSkill;
import com.project.codinviec_core_service.entity.LevelLanguage;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.mapper.LanguageSkillMapper;
import com.project.codinviec_core_service.repository.LanguageRepository;
import com.project.codinviec_core_service.repository.LanguageSkillRepository;
import com.project.codinviec_core_service.repository.LevelLanguageRepository;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.request.LanguageSkillRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.LanguageSkillService;
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
public class LanguageSkillServiceImp implements LanguageSkillService {

    private final LanguageSkillRepository languageSkillRepository;
    private final LanguageRepository languageRepository;
    private final LevelLanguageRepository levelLanguageRepository;
    private final UserRepository userRepository;
    private final LanguageSkillMapper languageSkillMapper;
    private final PageCustomHelper pageCustomHelper;

    @Override
    public List<LanguageSkillDTO> getAllLanguageSkill() {
        return languageSkillRepository.findAll()
                .stream()
                .map(languageSkillMapper::toDto)
                .toList();
    }

    @Override
    public Page<LanguageSkillDTO> getAllLanguageSkillPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom validated = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(validated.getPageNumber() - 1, validated.getPageSize());
        return languageSkillRepository.findAll(pageable).map(languageSkillMapper::toDto);
    }

    @Override
    public List<LanguageSkillDTO> getLanguageSkillByUser(String userId) {
        return languageSkillRepository.findByUser_Id(userId)
                .stream()
                .map(languageSkillMapper::toDto)
                .toList();
    }

    @Override
    public LanguageSkillDTO getLanguageSkillById(Integer id) {
        LanguageSkill ls = languageSkillRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id language_skill"));
        return languageSkillMapper.toDto(ls);
    }

    @Override
    @Transactional
    public LanguageSkillDTO createLanguageSkill(LanguageSkillRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id language"));
        LevelLanguage level = levelLanguageRepository.findById(request.getLevelLanguageId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id level_language"));

        if (languageSkillRepository.existsByUser_IdAndLanguage_Id(user.getId(), language.getId())) {
            throw new AppException(ResourceErrorCode.CONFLICT, "User đã có skill với language này");
        }

        LanguageSkill newSkill = languageSkillMapper.saveLanguageSkill(user, language, level, request);
        languageSkillRepository.save(newSkill);
        return languageSkillMapper.toDto(newSkill);
    }

    @Override
    @Transactional
    public LanguageSkillDTO updateLanguageSkill(int id, LanguageSkillRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        languageSkillRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id language_skill"));

        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id language"));
        LevelLanguage level = levelLanguageRepository.findById(request.getLevelLanguageId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id level_language"));

        LanguageSkill ls = languageSkillMapper.updateLanguageSkill(id, user, language, level, request);
        LanguageSkill updated = languageSkillRepository.save(ls);
        return languageSkillMapper.toDto(updated);
    }

    @Override
    @Transactional
    public LanguageSkillDTO deleteLanguageSkill(int id) {
        LanguageSkill ls = languageSkillRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id language_skill"));
        languageSkillRepository.delete(ls);
        return languageSkillMapper.toDto(ls);
    }
}
