package com.project.codinviec_auth_service.event.publish;

import com.project.codinviec_auth_service.entity.OutboxEventEntity;
import com.project.codinviec_auth_service.enums.EmailErrorCode;
import com.project.codinviec_auth_service.event.payload.CreateUserCorePayload;
import com.project.codinviec_auth_service.event.payload.UserRegisterEmailPayload;
import com.project.codinviec_auth_service.event.payload.VerifyRegisterPayload;
import com.project.codinviec_auth_service.exception.AppException;
import com.project.codinviec_auth_service.service.OutboxServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthEventPublisher {

    private final ObjectMapper objectMapper;
    private final OutboxServices outboxServices;
    private final OutboxPublisher outboxPublisher;

    public void publishUserRegisteredEmail(UserRegisterEmailPayload userRegisterEmailPayload) {
        try {
            String eventPayload = objectMapper.writeValueAsString(userRegisterEmailPayload);
            outboxServices.addEventToOutBox(OutboxEventEntity.builder()
                    .eventType("user-registered-email-topic")
                    .payload(eventPayload)
                    .status("PENDING")
                    .createdDate(LocalDateTime.now())
                    .build());
            outboxPublisher.markHasPendingEvents();
        } catch (RuntimeException e) {
            throw new AppException(EmailErrorCode.SEND_REGISTER_EMAIL_FAIL);
        }
    }

    public void publishVerifyRegister(VerifyRegisterPayload verifyRegisterPayload) {
        try {
            String eventPayload = objectMapper.writeValueAsString(verifyRegisterPayload);
            outboxServices.addEventToOutBox(OutboxEventEntity.builder()
                    .eventType("user-verify-topic")
                    .payload(eventPayload)
                    .status("PENDING")
                    .createdDate(LocalDateTime.now())
                    .build());
            outboxPublisher.markHasPendingEvents();
        } catch (RuntimeException e) {
            throw new AppException(EmailErrorCode.SEND_VERIFY_EMAIL_FAIL);
        }
    }

    public void publishUserRegisteredSuccess(CreateUserCorePayload createUserCorePayload) {
        try {
            String eventPayload = objectMapper.writeValueAsString(createUserCorePayload);
            outboxServices.addEventToOutBox(OutboxEventEntity.builder()
                    .eventType("user-registered-topic")
                    .payload(eventPayload)
                    .status("PENDING")
                    .createdDate(LocalDateTime.now())
                    .build());
            outboxPublisher.markHasPendingEvents();
        } catch (RuntimeException e) {
            throw new AppException(EmailErrorCode.USER_REGISTER_EVENT_FAIL);
        }
    }
}
