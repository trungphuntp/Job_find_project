package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.WardDTO;
import com.project.codinviec_core_service.entity.Ward;
import com.project.codinviec_core_service.mapper.WardMapper;
import com.project.codinviec_core_service.repository.WardRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.WardRequest;
import com.project.codinviec_core_service.service.WardService;
import com.project.codinviec_core_service.specification.WardSpecification;
import com.project.codinviec_core_service.util.helper.LocationHelper;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WardServiceImp implements WardService {
    private final WardRepository wardRepository;
    private final WardMapper wardMapper;
    private final PageCustomHelper pageCustomHelper;
    private final WardSpecification wardSpecification;
    private final LocationHelper locationHelper;

    @Override
    public List<WardDTO> getAll() {

        List<WardDTO> wardList = new ArrayList<>();

//        lấy dữ liệu từ redis
        List<WardDTO> wardRedis = locationHelper.getWardRedis();
        if (wardRedis.isEmpty()) {
            wardList = wardRepository.findAll()
                    .stream()
                    .map(wardMapper::toDTO)
                    .toList();
            locationHelper.addWardToRedis(wardList);
            return wardList;
        }
        return wardRedis;
    }

    @Override
    @Transactional
    public Page<WardDTO> getAllWithPage(PageRequestCustom req) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(req);

        //Search
        Specification<Ward> spec = wardSpecification.searchByName(pageRequestValidate.getKeyword());

        //Sort
        Sort sort = switch (pageRequestValidate.getSortBy()) {
            case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name");
            case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
            case "provinceNameAsc" -> Sort.by(Sort.Direction.ASC, "province.name");
            case "provinceNameDesc" -> Sort.by(Sort.Direction.DESC, "province.name");
            default -> Sort.by(Sort.Direction.ASC, "name");
        };

        //Page
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize(), sort);

        return wardRepository.findAll(spec, pageable)
                .map(wardMapper::toDTO);
    }

    @Override
    public WardDTO getById(int id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Ward ID: " + id));
        return wardMapper.toDTO(ward);
    }

    @Override
    @Transactional
    public WardDTO create(WardRequest request) {
        Ward entity = wardMapper.saveWard(request);
        return wardMapper.toDTO(wardRepository.save(entity));
    }

    @Override
    @Transactional
    public WardDTO update(int id, WardRequest request) {
        wardRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Ward ID: " + id));
        Ward entity = wardMapper.updateWard(id, request);
        return wardMapper.toDTO(wardRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(int id) {
        Ward ward = wardRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Ward ID: " + id));
        wardRepository.delete(ward);
    }
}
