package com.project.codinviec_core_service.event.publish;

import com.project.codinviec_core_service.entity.auth.OutboxEventEntity;
import com.project.codinviec_core_service.enums.CommonErrorCode;
import com.project.codinviec_core_service.event.payload.CreateUserCorePayload;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.service.OutboxServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserEventPublish {
    private final ObjectMapper objectMapper;
    private final OutboxServices outboxServices;
    private final OutboxPublisher outboxPublisher;

    public void publishRegisterSuccess(CreateUserCorePayload createUserCorePayload) {
        try {
            String eventPayload = objectMapper.writeValueAsString(createUserCorePayload);
            outboxServices.addEventToOutBox(OutboxEventEntity.builder()
                    .eventType("user-registered-success-topic")
                    .payload(eventPayload)
                    .status("PENDING")
                    .createdDate(LocalDateTime.now())
                    .build());
            outboxPublisher.markHasPendingEvents();
        } catch (RuntimeException e) {
            throw new AppException(CommonErrorCode.INTERNAL_SERVER_ERROR, "Tạo user thất bại");
        }
    }

    public void publishRegisterFail(CreateUserCorePayload createUserCorePayload) {
        try {
            String eventPayload = objectMapper.writeValueAsString(createUserCorePayload);
            outboxServices.addEventToOutBox(OutboxEventEntity.builder()
                    .eventType("user-registered-fail-topic")
                    .payload(eventPayload)
                    .status("PENDING")
                    .createdDate(LocalDateTime.now())
                    .build());
            outboxPublisher.markHasPendingEvents();
        } catch (RuntimeException e) {
            throw new AppException(CommonErrorCode.INTERNAL_SERVER_ERROR, "Tạo user thất bại");
        }
    }
}
