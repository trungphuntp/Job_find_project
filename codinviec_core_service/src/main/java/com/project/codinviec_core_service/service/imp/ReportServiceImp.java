package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.ReportDTO;
import com.project.codinviec_core_service.entity.Report;
import com.project.codinviec_core_service.entity.ReportStatus;
import com.project.codinviec_core_service.mapper.ReportMapper;
import com.project.codinviec_core_service.repository.ReportRepository;
import com.project.codinviec_core_service.repository.ReportStatusRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.ReportRequest;
import com.project.codinviec_core_service.service.ReportService;
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
public class ReportServiceImp implements ReportService {
    private final ReportRepository reportRepository;
    private final ReportStatusRepository reportStatusRepository;
    private final PageCustomHelper pageCustomHelper;
    private final ReportMapper reportMapper;

    @Override
    public List<ReportDTO> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(reportMapper::toDTO)
                .toList();
    }

    @Override
    public ReportDTO getReportById(int id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Report với ID: " + id));
        return reportMapper.toDTO(report);
    }

    @Override
    @Transactional
    public ReportDTO createReport(ReportRequest request) {
        ReportStatus status = reportStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy ReportStatus với ID: " + request.getStatusId()));

        Report entity = reportMapper.saveReport(request, status);
        return reportMapper.toDTO(reportRepository.save(entity));
    }

    @Override
    @Transactional
    public ReportDTO updateReport(int id, ReportRequest request) {
        reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Report với ID: " + id));

        ReportStatus status = reportStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy ReportStatus với ID: " + request.getStatusId()));

        Report entity = reportMapper.updateReport(id, request, status);
        return reportMapper.toDTO(reportRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteReport(int id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Report với ID: " + id));
        reportRepository.delete(report);
    }
    @Override
    public Page<ReportDTO> getAllReportsPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(pageRequestCustom);
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize());
        return reportRepository.findAll(pageable).map(reportMapper::toDTO);
    }
}
