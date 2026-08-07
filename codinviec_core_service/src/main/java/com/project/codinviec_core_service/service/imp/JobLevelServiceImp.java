package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.JobLevelDTO;
import com.project.codinviec_core_service.entity.JobLevel;
import com.project.codinviec_core_service.mapper.JobLevelMapper;
import com.project.codinviec_core_service.repository.JobLevelRepository;
import com.project.codinviec_core_service.request.JobLevelRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.JobLevelService;
import com.project.codinviec_core_service.specification.JobLevelSpecification;
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
public class JobLevelServiceImp implements JobLevelService {
    private final JobLevelRepository jobLevelRepository;
    private final JobLevelMapper jobLevelMapper;
    private final PageCustomHelper pageCustomHelper;
    private final JobLevelSpecification jobLevelSpecification;

    @Override
    public List<JobLevelDTO> getAll() {
        return jobLevelRepository.findAll()
                .stream()
                .map(jobLevelMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public Page<JobLevelDTO> getAllWithPage(PageRequestCustom req) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(req);

        //Search
        Specification<JobLevel> spec = jobLevelSpecification.searchByName(pageRequestValidate.getKeyword());

        //Sort
        Sort sort = switch (pageRequestValidate.getSortBy()) {
            case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name");
            case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };

        //Page
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize(), sort);

        return jobLevelRepository.findAll(spec, pageable)
                .map(jobLevelMapper::toDTO);
    }

    @Override
    public JobLevelDTO getById(int id) {
        JobLevel jobLevel = jobLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Joblevel với ID: " + id));
        return jobLevelMapper.toDTO(jobLevel);
    }

    @Override
    @Transactional
    public JobLevelDTO create(JobLevelRequest request){
        JobLevel entity = jobLevelMapper.saveJobLevel(request);
        return jobLevelMapper.toDTO(jobLevelRepository.save(entity));
    }

    @Override
    @Transactional
    public JobLevelDTO update(int id, JobLevelRequest request){
        jobLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Joblevel ID: " + id));
        JobLevel entity = jobLevelMapper.updateJobLevel(id, request);
        return jobLevelMapper.toDTO(jobLevelRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(int id) {
        JobLevel jobLevel = jobLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Joblevel ID: " + id));
        jobLevelRepository.delete(jobLevel);
    }
}
