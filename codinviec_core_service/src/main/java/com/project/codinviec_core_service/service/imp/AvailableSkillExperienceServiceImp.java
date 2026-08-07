package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.AvailableSkillExperienceDTO;
import com.project.codinviec_core_service.entity.AvailableSkill;
import com.project.codinviec_core_service.entity.AvailableSkillExperience;
import com.project.codinviec_core_service.entity.Experience;
import com.project.codinviec_core_service.entity.GroupCoreSkill;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.mapper.AvailableSkillExperienceMapper;
import com.project.codinviec_core_service.repository.AvailableSkillExperienceRepository;
import com.project.codinviec_core_service.repository.AvailableSkillRepository;
import com.project.codinviec_core_service.repository.ExperienceRepository;
import com.project.codinviec_core_service.repository.GroupCoreSkillRepository;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.request.AvailableSkillExperienceRequest;
import com.project.codinviec_core_service.request.DeleteAvailableSkillExperienceByGroupCoreIdRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.AvailableSkillExperienceService;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailableSkillExperienceServiceImp implements AvailableSkillExperienceService {

    private final AvailableSkillExperienceRepository availableSkillExperienceRepository;
    private final GroupCoreSkillRepository groupCoreSkillRepository;
    private final AvailableSkillRepository availableSkillRepository;
    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;
    private final AvailableSkillExperienceMapper mapper;
    private final PageCustomHelper pageCustomHelper;

    @Override
    public List<AvailableSkillExperienceDTO> getAllAvailableSkillExperience() {
        return availableSkillExperienceRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Page<AvailableSkillExperienceDTO> getAllAvailableSkillExperiencePage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom validated = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(validated.getPageNumber() - 1, validated.getPageSize());
        return availableSkillExperienceRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public List<AvailableSkillExperienceDTO> getAvailableSkillExperienceByUser(String userId) {
        return availableSkillExperienceRepository.findByUser_Id(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public AvailableSkillExperienceDTO getAvailableSkillExperienceById(Integer id) {
        AvailableSkillExperience ase = availableSkillExperienceRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id available_skill_experience"));
        return mapper.toDto(ase);
    }

    @Override
    @Transactional
    public AvailableSkillExperienceDTO createAvailableSkillExperience(AvailableSkillExperienceRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));

        GroupCoreSkill groupCoreSkill = null;
        if (request.getGroupCoreId() != null) {
            groupCoreSkill = groupCoreSkillRepository.findById(request.getGroupCoreId())
                    .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id group_core_skill"));
        }

        AvailableSkill availableSkill = availableSkillRepository.findById(request.getAvailableSkillId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id available_skill"));

        Experience experience = experienceRepository.findById(request.getExperienceId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id experience"));

//        // chặn trùng
//        if (availableSkillExperienceRepository.existsByUser_IdAndAvailableSkill_IdAndExperience_Id(
//                user.getId(), availableSkill.getId(), experience.getId())) {
//            throw new AppException(ResourceErrorCode.NOT_FOUND, "User đã tồn tại available_skill + experience này");
//        }

        AvailableSkillExperience ase = mapper.saveAvailableSkillExperience(user, groupCoreSkill, availableSkill, experience, request);

        availableSkillExperienceRepository.save(ase);
        return mapper.toDto(ase);
    }

    @Override
    @Transactional
    public AvailableSkillExperienceDTO updateAvailableSkillExperience(int id, AvailableSkillExperienceRequest request) {
        AvailableSkillExperience ase = availableSkillExperienceRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id available_skill_experience"));

        if (request.getGroupCoreId() != null) {
            GroupCoreSkill groupCoreSkill = groupCoreSkillRepository.findById(request.getGroupCoreId())
                    .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id group_core_skill"));
            ase.setGroupCoreSkill(groupCoreSkill);
        }

        if (request.getAvailableSkillId() != null) {
            AvailableSkill availableSkill = availableSkillRepository.findById(request.getAvailableSkillId())
                    .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id available_skill"));
            ase.setAvailableSkill(availableSkill);
        }

        if (request.getExperienceId() != null) {
            Experience experience = experienceRepository.findById(request.getExperienceId())
                    .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id experience"));
            ase.setExperience(experience);
        }

        AvailableSkillExperience updated = availableSkillExperienceRepository.save(ase);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public AvailableSkillExperienceDTO deleteAvailableSkillExperience(int id) {
        AvailableSkillExperience ase = availableSkillExperienceRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id available_skill_experience"));
        availableSkillExperienceRepository.delete(ase);
        return mapper.toDto(ase);
    }

    @Override
    @Transactional
    public List<AvailableSkillExperienceDTO> deleteAvailableSkillExperienceByGroupCoreId(DeleteAvailableSkillExperienceByGroupCoreIdRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));

        GroupCoreSkill groupCoreSkill = groupCoreSkillRepository.findById(request.getGroupCoreId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id group_core_skill"));

        List<AvailableSkillExperience> skillsUser = availableSkillExperienceRepository.findByUser_Id(user.getId());

        Iterator<AvailableSkillExperience> iterator = skillsUser.iterator();
        while (iterator.hasNext()) {
            AvailableSkillExperience skill = iterator.next();
            if (skill.getGroupCoreSkill().getId() == groupCoreSkill.getId()) {
                availableSkillExperienceRepository.deleteById(skill.getId());
                groupCoreSkillRepository.deleteById(skill.getGroupCoreSkill().getId());
                iterator.remove();
            }
        }
        return skillsUser.stream().map(mapper::toDto).toList();
    }
}
