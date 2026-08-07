package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.LanguageDTO;
import com.project.codinviec_core_service.entity.Language;
import com.project.codinviec_core_service.mapper.LanguageMapper;
import com.project.codinviec_core_service.repository.LanguageRepository;
import com.project.codinviec_core_service.request.LanguageRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.LanguageService;
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
public class LanguageServiceImp implements LanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;
    private final PageCustomHelper pageCustomHelper;

    @Override
    public List<LanguageDTO> getAllLanguage() {
        return languageRepository.findAll()
                .stream()
                .map(languageMapper::toDto)
                .toList();
    }

    @Override
    public Page<LanguageDTO> getAllLanguagePage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize());
        return languageRepository.findAll(pageable).map(languageMapper::toDto);
    }

    @Override
    public LanguageDTO getLanguageById(Integer id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id language"));
        return languageMapper.toDto(language);
    }

    @Override
    @Transactional
    public LanguageDTO createLanguage(LanguageRequest languageRequest) {
        Language language = languageMapper.saveLanguage(languageRequest);
        languageRepository.save(language);
        return languageMapper.toDto(language);
    }

    @Override
    @Transactional
    public LanguageDTO updateLanguage(int id, LanguageRequest languageRequest) {
        languageRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id language"));

        Language entity = languageMapper.updateLanguage(id, languageRequest);
        Language updatedLanguage = languageRepository.save(entity);
        return languageMapper.toDto(updatedLanguage);
    }

    @Override
    @Transactional
    public LanguageDTO deleteLanguage(int id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id language"));
        languageRepository.delete(language);
        return languageMapper.toDto(language);
    }
}
