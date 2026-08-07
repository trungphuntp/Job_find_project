package com.project.codinviec_core_service.service.imp;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.dto.ReviewDTO;
import com.project.codinviec_core_service.dto.UserReviewDTO;
import com.project.codinviec_core_service.entity.Review;
import com.project.codinviec_core_service.entity.auth.Company;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.mapper.ReviewMapper;
import com.project.codinviec_core_service.repository.ReviewRepository;
import com.project.codinviec_core_service.repository.auth.CompanyRepository;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.SaveUpdateReviewRequest;
import com.project.codinviec_core_service.service.ReviewService;
import com.project.codinviec_core_service.specification.ReviewSpecification;

import com.project.codinviec_core_service.util.helper.PageCustomHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImp implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final PageCustomHelper pageCustomHelper;
    private final ReviewSpecification reviewSpecification;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(review -> reviewMapper.reviewToReviewDTO(review, true))
                .toList();
    }

    @Override
    public Page<ReviewDTO> getAllReviewsPage(PageRequestCustom pageRequestCustom) {
        // Validate pageCustom
        PageRequestCustom pageRequestValidate = pageCustomHelper.validatePageCustom(pageRequestCustom);

        // Tạo page cho api
        Pageable pageable = PageRequest.of(pageRequestValidate.getPageNumber() - 1, pageRequestValidate.getPageSize());

        // Tạo search
        Specification<Review> spec = Specification.allOf(reviewSpecification.searchByName(pageRequestValidate.getKeyword()));
        return reviewRepository.findAll(spec, pageable)
                .map(review -> reviewMapper.reviewToReviewDTO(review, true));
    }

    @Override
    public ReviewDTO getReviewById(Integer id) {
        Review review= reviewRepository.findById(id).orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id review"));
        return reviewMapper.reviewToReviewDTO(review, true);
    }

    @Override
    @Transactional
    public ReviewDTO saveReview(SaveUpdateReviewRequest saveUpdateReviewRequest) {
        User user = userRepository.findById(saveUpdateReviewRequest.getUserId())
                .orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        Company company = companyRepository.findById(saveUpdateReviewRequest.getCompanyId())
                .orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company"));
        Review review = reviewMapper.saveReviewMapper(user,company,saveUpdateReviewRequest);
        return reviewMapper.reviewToReviewDTO(reviewRepository.save(review), true);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(Integer reviewId, SaveUpdateReviewRequest saveUpdateReviewRequest) {
        reviewRepository.findById(reviewId)
                .orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id review"));
        User user = userRepository.findById(saveUpdateReviewRequest.getUserId())
                .orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        Company company = companyRepository.findById(saveUpdateReviewRequest.getCompanyId())
                .orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company"));

        Review mapperReview = reviewMapper.updateReviewMapper(reviewId ,user,company,saveUpdateReviewRequest);
        return reviewMapper.reviewToReviewDTO(reviewRepository.save(mapperReview), true);
    }

    @Override
    @Transactional
    public ReviewDTO deleteReview(Integer reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id review"));
        reviewRepository.delete(review);
        return reviewMapper.reviewToReviewDTO(review,true);
    }

    @Override
    public UserReviewDTO getReviewsByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id user"));
        List<ReviewDTO> listReview = user.getListReview().stream().map(review -> reviewMapper.reviewToReviewDTO(review , false)).toList();
        return reviewMapper.reviewToUserReviewDTO(user,listReview);
    }

    @Override
    public List<ReviewDTO> getReviewsByCompanyId(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(()->new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id company"));
        return company.getListReview().stream().map(review -> reviewMapper.reviewToReviewDTO(review,true)).toList();
    }
}
