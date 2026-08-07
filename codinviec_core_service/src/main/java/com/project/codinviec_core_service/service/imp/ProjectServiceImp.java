package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.ProjectDTO;
import com.project.codinviec_core_service.entity.Project;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.mapper.ProjectMapper;
import com.project.codinviec_core_service.repository.ProjectRepository;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.ProjectRequest;
import com.project.codinviec_core_service.service.ProjectService;
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
public class ProjectServiceImp implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final PageCustomHelper pageCustomHelper;

    @Override
    public List<ProjectDTO> getAllProject() {
        return projectRepository.findAll()
                .stream()
                .map(projectMapper::toDto)
                .toList();
    }

    @Override
    public Page<ProjectDTO> getAllProjectPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize());

        return projectRepository.findAll(pageable).map(projectMapper::toDto);
    }

    @Override
    public ProjectDTO getProjectById(Integer id) {
        Project project = projectRepository.findById(id).orElseThrow(()-> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id project"));
        return projectMapper.toDto(project);
    }

    @Override
    @Transactional
    public ProjectDTO createProject(ProjectRequest projectRequest) {
        User user = userRepository.findById(projectRequest.getUserId())
                .orElseThrow(()-> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        Project project = projectMapper.saveProject(user, projectRequest);
        projectRepository.save(project);

        return projectMapper.toDto(project);
    }

    @Override
    @Transactional
    public ProjectDTO updateProject(int id, ProjectRequest projectRequest) {
        User user = userRepository.findById(projectRequest.getUserId())
                .orElseThrow(()-> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        projectRepository.findById(id)
                .orElseThrow(()-> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id project"));

        Project project = projectMapper.updateProject(id, user, projectRequest);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toDto(updatedProject);
    }

    @Override
    @Transactional
    public ProjectDTO deleteProject(int id) {
        Project project = projectRepository.findById(id).orElseThrow(()-> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id project"));
        projectRepository.delete(project);
        return projectMapper.toDto(project);
    }
}
