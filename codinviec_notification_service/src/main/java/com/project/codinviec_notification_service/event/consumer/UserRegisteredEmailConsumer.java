package com.project.codinviec_notification_service.event.consumer;

import com.project.codinviec_notification_service.enums.NotificationErrorCode;
import com.project.codinviec_notification_service.event.payload.UserRegisterEmailPayload;
import com.project.codinviec_notification_service.exception.AppException;
import com.project.codinviec_notification_service.service.EmailTemplateService;
import com.project.codinviec_notification_service.service.Imp.EmailServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class UserRegisteredEmailConsumer {

    private final EmailServiceImp emailService;
    private final EmailTemplateService emailTemplateService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "user-registered-email-topic",
            groupId = "notification-service.user-registered-email.success"
    )
    public void handleUserRegistered(String message,
                                     Acknowledgment ack) {
        try {
            UserRegisterEmailPayload userRegisterEmailPayload = objectMapper.readValue(message, UserRegisterEmailPayload.class);

            String html = emailTemplateService.buildRegisterTemplateEmail(userRegisterEmailPayload.getFirstName() + " " + userRegisterEmailPayload.getLastName());
            emailService.sendEmailRegister(
                    userRegisterEmailPayload.getEmail(),
                    "Tin báo đăng ký tài khoản thành công!",
                    html);
            ack.acknowledge();
        } catch (RuntimeException e) {
            throw new AppException(NotificationErrorCode.SEND_REGISTER_EMAIL_FAIL);
        }
    }
}
