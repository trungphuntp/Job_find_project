package com.project.codinviec_core_service.service.imp;


import com.project.codinviec_core_service.dto.WishlistCandidateDTO;
import com.project.codinviec_core_service.entity.WishlistCandidate;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.entity.key.WishlistCandidateKey;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.mapper.WishlistCandidateMapper;
import com.project.codinviec_core_service.repository.WishlistCandidateRepository;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.WishlistCandidateRequest;
import com.project.codinviec_core_service.service.WishlistCandidateService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistCandidateServiceImp implements WishlistCandidateService {
    private final WishlistCandidateRepository wishlistCandidateRepository;
    private final WishlistCandidateMapper wishlistCandidateMapper;
    private final UserRepository userRepository;

    @Override
    public List<WishlistCandidateDTO> getAllWishlistCandidates() {
        return wishlistCandidateMapper.mappedToWishlistCandidateDTO(wishlistCandidateRepository.findAll());
    }

    @Override
    public Page<WishlistCandidateDTO> getAllWishlistCandidatesPage(PageRequestCustom pageRequestCustom) {
        if (pageRequestCustom.getPageSize() == 0) {
            throw new AppException(ResourceErrorCode.INVALID_PARAM, "page size truyền lên không hợp lệ");
        }
        Pageable pageable = PageRequest.of(pageRequestCustom.getPageNumber() - 1, pageRequestCustom.getPageSize());
        Page<WishlistCandidate> wishlistCandidatePage = wishlistCandidateRepository.findAll(pageable);
        List<WishlistCandidateDTO> wishlistCandidates = wishlistCandidateMapper.mappedToWishlistCandidateDTO(wishlistCandidatePage.getContent());
        return new PageImpl<>(wishlistCandidates, pageable, wishlistCandidatePage.getTotalElements());
    }

    @Override
    public List<WishlistCandidateDTO> getWishlistCandidateByHrId(String hrId) {
        userRepository.findById(hrId)
                .orElseThrow(() -> new AppException(ResourceErrorCode.INVALID_PARAM, "Không tìm thấy hr id"));
        return wishlistCandidateMapper.mappedToWishlistCandidateDTO(wishlistCandidateRepository.findByUserHr_Id(hrId));
    }

    @Override
    @Transactional
    public List<WishlistCandidateDTO> saveWishlistCandidate(WishlistCandidateRequest wishlistCandidateRequest) {
        if (wishlistCandidateRequest.getHrId().equalsIgnoreCase(wishlistCandidateRequest.getCandidateId())) {
            throw new AppException(ResourceErrorCode.INVALID_PARAM, "Tham số truyền vào cùng 1 người không thể wishlist");
        }
        User userHr = userRepository.findById(wishlistCandidateRequest.getHrId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id Hr"));

        User userCandidate = userRepository.findById(wishlistCandidateRequest.getCandidateId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy id candidate"));

        WishlistCandidate wc = wishlistCandidateMapper.saveWishlistCandidate(userHr, userCandidate, wishlistCandidateRequest);
        wishlistCandidateRepository.save(wc);
        return wishlistCandidateMapper.mappedToWishlistCandidateDTO(wishlistCandidateRepository.findByUserHr_Id(userHr.getId()));
    }

    @Override
    @Transactional
    public List<WishlistCandidateDTO> deleteWistListCandidate(WishlistCandidateRequest wishlistCandidateRequest) {
        WishlistCandidateKey wishlistCandidateKey = new WishlistCandidateKey(wishlistCandidateRequest.getHrId(), wishlistCandidateRequest.getCandidateId());
        WishlistCandidate wishlistCandidate = wishlistCandidateRepository.findById(wishlistCandidateKey)
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy wishlist candidate id"));
        wishlistCandidateRepository.delete(wishlistCandidate);
        List<WishlistCandidate> getDeleteWc = wishlistCandidateRepository.findByUserHr_Id(wishlistCandidateKey.getHrId());
        return wishlistCandidateMapper.mappedToWishlistCandidateDTO(getDeleteWc);
    }
}
