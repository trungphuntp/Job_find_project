package com.project.codinviec_core_service.service.imp.auth;

import com.project.codinviec_core_service.dto.auth.RoleDTO;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.entity.auth.Role;
import com.project.codinviec_core_service.mapper.auth.RoleMapper;
import com.project.codinviec_core_service.repository.auth.RoleRepository;
import com.project.codinviec_core_service.request.auth.RoleRequest;
import com.project.codinviec_core_service.service.auth.RoleService;
import com.project.codinviec_core_service.specification.auth.RoleSpecification;
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
public class RoleServiceImp implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PageCustomHelper pageCustomHelper;
    private final RoleSpecification roleSpecification;

    @Override
    public List<RoleDTO> getAll() {
        return roleMapper.toRoleDTOList(roleRepository.findAll());
    }

    @Override
    @Transactional
    public Page<RoleDTO> getAllWithPage(PageRequestCustom req) {

        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(req);
        Specification<Role> spec = roleSpecification.searchByName(pageRequestValidate.getKeyword());

        Sort sort = switch (pageRequestValidate.getSortBy()) {
            case "roleNameAsc" -> Sort.by(Sort.Direction.ASC, "roleName");
            case "roleNameDesc" -> Sort.by(Sort.Direction.DESC, "roleName");
            case "descriptionAsc" -> Sort.by(Sort.Direction.ASC, "description");
            case "descriptionDesc" -> Sort.by(Sort.Direction.DESC, "description");
            case "createdDateDesc" -> Sort.by(Sort.Direction.DESC, "createdDate");
            case "updatedDateAsc" -> Sort.by(Sort.Direction.ASC, "updatedDate");
            case "updatedDateDesc" -> Sort.by(Sort.Direction.DESC, "updatedDate");
            default -> Sort.by(Sort.Direction.ASC, "id");
        };

        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize(), sort);

        return roleRepository.findAll(spec,pageable)
                .map(roleMapper::toRoleDTO);
    }

    @Override
    public RoleDTO getById(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy role Id"));
        return roleMapper.toRoleDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO create(RoleRequest req) {
        roleRepository.findByRoleNameIgnoreCase(req.getRoleName())
                .ifPresent(r -> { throw new AppException(ResourceErrorCode.CONFLICT, "Role đã tồn tại!"); });

        Role role = roleMapper.toCreateRole(req);
        return roleMapper.toRoleDTO(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleDTO update(String id, RoleRequest req) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy role Id"));
        roleMapper.toUpdateRole(req, existingRole);
        return roleMapper.toRoleDTO(roleRepository.save(existingRole));
    }


    @Override
    @Transactional
    public RoleDTO deleteById(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy role ID"));
        roleRepository.delete(role);
        return roleMapper.toRoleDTO(role);
    }
}
