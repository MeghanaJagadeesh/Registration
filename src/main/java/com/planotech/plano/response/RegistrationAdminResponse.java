package com.planotech.plano.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class RegistrationAdminResponse {

    private Long registrationId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime submittedAt;
    private Map<String, Object> responses;
}

