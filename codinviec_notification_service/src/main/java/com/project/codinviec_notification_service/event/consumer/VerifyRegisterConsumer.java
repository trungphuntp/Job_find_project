package com.project.codinviec_notification_service.event.consumer;

import com.project.codinviec_notification_service.enums.NotificationErrorCode;
import com.project.codinviec_notification_service.event.payload.VerifyRegisterPayload;
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
public class VerifyRegisterConsumer {
    private final EmailServiceImp emailService;
    private final EmailTemplateService emailTemplateService;
    private final ObjectMapper objectMapper;


    @KafkaListener(
            topics = "user-verify-topic",
            groupId = "notification-service.user-email.verify"
    )
    public void handleUserRegistered(String message,
                                     Acknowledgment ack) {
        try {
            VerifyRegisterPayload verifyRegisterPayload = objectMapper.readValue(message, VerifyRegisterPayload.class);
            String html = emailTemplateService.buildOtpTemplateEmail(verifyRegisterPayload.getOtp());
            emailService.sendEmailRegister(
                    verifyRegisterPayload.getEmail(),
                    "Thông báo xác thực tài khoản!",
                    html);
            ack.acknowledge();
        } catch (RuntimeException e) {
            throw new AppException(NotificationErrorCode.SEND_VERIFY_EMAIL_FAIL);
        }
    }
}
