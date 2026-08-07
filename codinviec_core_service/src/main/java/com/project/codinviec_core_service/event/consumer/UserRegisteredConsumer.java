package com.project.codinviec_core_service.event.consumer;

import com.project.codinviec_core_service.enums.CommonErrorCode;
import com.project.codinviec_core_service.event.payload.CreateUserCorePayload;
import com.project.codinviec_core_service.event.publish.UserEventPublish;
import com.project.codinviec_core_service.exception.AppException;
import com.project.codinviec_core_service.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final UserEventPublish userEventPublish;

    @KafkaListener(
            topics = "user-registered-topic",
            groupId = "core-service.user-registered.created"
    )
    public void handleUserRegistered(String message, Acknowledgment ack) {

        CreateUserCorePayload payload;
        try {
            payload = objectMapper.readValue(message, CreateUserCorePayload.class);
        } catch (RuntimeException e) {
            ack.acknowledge();
            return;
        }

        try {
            userService.registeredUser(payload);
            userEventPublish.publishRegisterSuccess(payload);
            ack.acknowledge();

        } catch (Exception e) {
            e.printStackTrace();
            userEventPublish.publishRegisterFail(payload);
            ack.acknowledge();
            throw new AppException(CommonErrorCode.INTERNAL_SERVER_ERROR, "Tạo user thất bại");
        }
    }
}
