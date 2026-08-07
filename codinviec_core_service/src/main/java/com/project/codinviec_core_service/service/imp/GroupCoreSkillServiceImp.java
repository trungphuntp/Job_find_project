package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.GroupCoreSkillDTO;
import com.project.codinviec_core_service.entity.GroupCoreSkill;
import com.project.codinviec_core_service.mapper.GroupCoreSkillMapper;
import com.project.codinviec_core_service.repository.GroupCoreSkillRepository;
import com.project.codinviec_core_service.request.GroupCoreSkillRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.GroupCoreSkillService;
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
public class GroupCoreSkillServiceImp implements GroupCoreSkillService {

    private final GroupCoreSkillRepository repository;
    private final GroupCoreSkillMapper mapper;
    private final PageCustomHelper pageCustomHelper;

    @Override
    public List<GroupCoreSkillDTO> getAllGroupCoreSkill() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Page<GroupCoreSkillDTO> getAllGroupCoreSkillPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom validated = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(validated.getPageNumber() - 1, validated.getPageSize());
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public GroupCoreSkillDTO getGroupCoreSkillById(Integer id) {
        GroupCoreSkill entity = repository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id group_core_skill"));
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public GroupCoreSkillDTO createGroupCoreSkill(GroupCoreSkillRequest request) {
        GroupCoreSkill entity = mapper.saveGroupCoreSkill(request);
        repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    @Transactional
    public GroupCoreSkillDTO updateGroupCoreSkill(int id, GroupCoreSkillRequest request) {
        repository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id group_core_skill"));

        GroupCoreSkill entity = mapper.updateGroupCoreSkill(id, request);
        GroupCoreSkill updated = repository.save(entity);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public GroupCoreSkillDTO deleteGroupCoreSkill(int id) {
        GroupCoreSkill entity = repository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id group_core_skill"));
        repository.delete(entity);
        return mapper.toDto(entity);
    }
}
