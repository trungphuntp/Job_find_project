package com.project.codinviec_auth_service.service.Imp;

import com.project.codinviec_auth_service.entity.UserEntity;
import com.project.codinviec_auth_service.enums.EmailErrorCode;
import com.project.codinviec_auth_service.exception.AppException;
import com.project.codinviec_auth_service.repository.UserRepository;
import com.project.codinviec_auth_service.service.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServicesImp implements UserServices {

    private final UserRepository userRepository;

    @Override
    public void deleteUser(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(EmailErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(EmailErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }
}
