package com.project.codinviec_core_service.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String path;
    private Instant timestamp;
}
