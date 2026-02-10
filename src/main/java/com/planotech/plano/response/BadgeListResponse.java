package com.planotech.plano.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class BadgeListResponse {

    private Long entryId;

    private String name;
    private String email;
    private String phone;

    private String badgeCode;
    private String qrUrl;

    private LocalDateTime submittedAt;

    private Map<String, Object> responses;
}

