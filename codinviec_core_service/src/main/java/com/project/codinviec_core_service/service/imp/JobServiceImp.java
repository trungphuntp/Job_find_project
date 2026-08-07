package com.project.codinviec_core_service.service.imp;

import com.project.codinviec_core_service.dto.JobDTO;
import com.project.codinviec_core_service.entity.*;
import com.project.codinviec_core_service.entity.auth.Company;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.entity.key.JobUserKey;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.mapper.AvailableSkillMapper;
import com.project.codinviec_core_service.mapper.JobMapper;
import com.project.codinviec_core_service.mapper.StatusSpecialMapper;
import com.project.codinviec_core_service.mapper.auth.UserMapper;
import com.project.codinviec_core_service.repository.AvailableSkillsJobRepository;
import com.project.codinviec_core_service.repository.JobRepository;
import com.project.codinviec_core_service.repository.JobUserRepository;
import com.project.codinviec_core_service.repository.StatusSpecialJobRepository;
import com.project.codinviec_core_service.repository.auth.CompanyRepository;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.request.ApplyJobRequest;
import com.project.codinviec_core_service.request.GetJobFeaturedRequest;
import com.project.codinviec_core_service.request.JobFilterRequest;
import com.project.codinviec_core_service.request.JobRequest;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.service.JobService;
import com.project.codinviec_core_service.specification.JobSpecification;
import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImp implements JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final CompanyRepository companyRepository;
    private final PageCustomHelper pageCustomHelper;
    private final JobSpecification jobSpecification;

    private final StatusSpecialJobRepository statusSpecialJobRepository;
    private final StatusSpecialMapper statusSpecialMapper;

    private final AvailableSkillsJobRepository availableSkillsJobRepository;
    private final AvailableSkillMapper availableSkillMapper;

    private final UserRepository userRepository;
    private final JobUserRepository  jobUserRepository;

    @Override
    public List<JobDTO> getAllJob() {
        List<Job> jobs = jobRepository.findAll();
        List<JobDTO> jobDTOList = jobs.stream().map(jobMapper::toDTO).toList();
        enrichJobDTOs(jobDTOList);
        return jobDTOList;
    }

    @Override
    public Page<JobDTO> getAllJobPage(PageRequestCustom pageRequestCustom) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(pageRequestCustom);

        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize());

        Specification<Job> spec = Specification.allOf(jobSpecification.searchByName(pageRequestValidate.getKeyword()));

        Page<Job> jobPage = jobRepository.findAll(spec, pageable);
        List<JobDTO> jobDTOs = jobPage.getContent().stream().map(jobMapper::toDTO).toList();
        enrichJobDTOs(jobDTOs);
        return new org.springframework.data.domain.PageImpl<>(jobDTOs, pageable, jobPage.getTotalElements());
    }

    @Override
    public Page<JobDTO> getAllJobPageWithFilter(JobFilterRequest jobFilterRequest) {
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(PageRequestCustom.builder()
                .pageSize(jobFilterRequest.getPageSize())
                .pageNumber(jobFilterRequest.getPageNumber())
                .keyword(jobFilterRequest.getKeyword())
                .build());
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize());
        String keyword = jobFilterRequest.getKeyword() != null ? jobFilterRequest.getKeyword().trim() : "";
        Specification<Job> spec = Specification.allOf(
                jobSpecification.filterJob(
                        jobFilterRequest.getProvinceName(),
                        jobFilterRequest.getIndustryNames(),
                        jobFilterRequest.getJobLevelNames(),
                        jobFilterRequest.getEmploymentTypeNames(),
                        jobFilterRequest.getSalaryMin(),
                        jobFilterRequest.getSalaryMax()
                ),
                jobSpecification.searchByKeyword(keyword));
        Page<Job> jobPage = jobRepository.findAll(spec, pageable);
        List<JobDTO> jobDTOs = jobPage.getContent().stream().map(jobMapper::toDTO).toList();
        enrichJobDTOs(jobDTOs);
        return new org.springframework.data.domain.PageImpl<>(jobDTOs, pageable, jobPage.getTotalElements());
    }

    @Override
    public JobDTO getJobById(int id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Job ID: " + id));
        List<JobDTO> list = List.of(jobMapper.toDTO(job));
        enrichJobDTOs(list);
        return list.get(0);
    }

    @Override
    public List<JobDTO> getJobByIdCompany(String companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy company"));
        List<JobDTO> jobDTOList = jobRepository.getJobByCompany_Id(companyId)
                .stream().map(jobMapper::toDTO).toList();
        enrichJobDTOs(jobDTOList);
        return jobDTOList;
    }

    @Override
    @Transactional
    public JobDTO createJob(JobRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id Company"));

        Job job = Job.builder()
                .jobPosition(request.getJobPosition())
                .company(company)
                .detailAddress(request.getDetailAddress())
                .descriptionJob(request.getDescriptionJob())
                .requirement(request.getRequirement())
                .benefits(request.getBenefits())
                .province(Province.builder().id(request.getProvinceId()).build())
                .industry(Industry.builder().id(request.getIndustryId()).build())
                .jobLevel(JobLevel.builder().id(request.getJobLevelId()).build())
                .degreeLevel(DegreeLevel.builder().id(request.getDegreeLevelId()).build())
                .employmentType(EmploymentType.builder().id(request.getEmploymentTypeId()).build())
                .experience(Experience.builder().id(request.getExperienceId()).build())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
        return jobMapper.toDTO(jobRepository.save(job));
    }

    @Override
    @Transactional
    public JobDTO updateJob(int id, JobRequest request) {
        jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Job ID: " + id));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id Company"));

        Job job = Job.builder()
                .id(id)
                .jobPosition(request.getJobPosition())
                .company(company)
                .detailAddress(request.getDetailAddress())
                .descriptionJob(request.getDescriptionJob())
                .requirement(request.getRequirement())
                .benefits(request.getBenefits())
                .province(Province.builder().id(request.getProvinceId()).build())
                .industry(Industry.builder().id(request.getIndustryId()).build())
                .jobLevel(JobLevel.builder().id(request.getJobLevelId()).build())
                .degreeLevel(DegreeLevel.builder().id(request.getDegreeLevelId()).build())
                .employmentType(EmploymentType.builder().id(request.getEmploymentTypeId()).build())
                .experience(Experience.builder().id(request.getExperienceId()).build())
                .createdDate(request.getCreatedDate())
                .updatedDate(LocalDateTime.now())
                .build();
        return jobMapper.toDTO(jobRepository.save(job));
    }

    @Override
    @Transactional
    public void deleteJob(int id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Job ID: " + id));
        jobRepository.delete(job);
    }

    @Override
    public List<JobDTO> getFeaturedJobs(GetJobFeaturedRequest request) {
        int limit = request.getLimit() > 0 ? request.getLimit() : 8;
        List<Integer> ids = jobRepository.findFeaturedJobIdsRandom(limit);
        if (ids.isEmpty()) return List.of();
        List<Job> jobs = jobRepository.findByIdInWithAssociations(ids);
        List<JobDTO> jobDTOs = jobs.stream().map(jobMapper::toDTO).toList();
        enrichJobDTOs(jobDTOs);
        return jobDTOs;
    }

    private void enrichJobDTOs(List<JobDTO> jobDTOs) {
        List<Integer> ids = jobDTOs.stream().map(JobDTO::getId).toList();
        Map<Integer, List<StatusSpecialJob>> statusMap = statusSpecialJobRepository.findByJob_IdIn(ids)
                .stream().collect(Collectors.groupingBy(s -> s.getJob().getId()));
        Map<Integer, List<AvailableSkillsJob>> skillMap = availableSkillsJobRepository.findByJob_IdIn(ids)
                .stream().collect(Collectors.groupingBy(a -> a.getJob().getId()));
        for (JobDTO jobDTO : jobDTOs) {
            jobDTO.setStatusSpecials(statusSpecialMapper.StatusSpecialJobToStatusSpecialDTO(
                    statusMap.getOrDefault(jobDTO.getId(), List.of())));
            jobDTO.setSkills(availableSkillMapper.AvailbleSkillJobToAvaibleSkill(
                    skillMap.getOrDefault(jobDTO.getId(), List.of())));
        }
    }

    @Override
    @Transactional
    public JobDTO applyJob(ApplyJobRequest applyJobRequest) {
        Job job = jobRepository.findById(applyJobRequest.getIdJob())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Job ID: " + applyJobRequest.getIdJob()));
        User user = userRepository.findById(applyJobRequest.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        if (user.getCv() == null){
            throw new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy CV của User");
        }
        JobUser existed = jobUserRepository.findById(new JobUserKey(job.getId(), user.getId())).orElse(null);
        if (existed != null) {
            throw new AppException(ResourceErrorCode.CONFLICT, "Bạn đã apply công việc này rồi");
        }
        jobUserRepository.save(JobUser.builder().id(JobUserKey.builder()
                .jobId(job.getId()).userId(user.getId()).build())
                .job(job).user(user).build());
        return getJobById(job.getId());
    }
}
