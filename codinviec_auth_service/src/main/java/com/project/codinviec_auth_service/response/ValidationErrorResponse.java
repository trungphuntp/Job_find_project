package com.project.codinviec_auth_service.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ValidationErrorResponse {
    private String code;
    private String message;
    private String path;
    private Instant timestamp;
    private List<FieldError> errors;

    @Getter
    @Builder
    public static class FieldError {
        private String field;
        private String rejectedValue;
        private String reason;
    }
}