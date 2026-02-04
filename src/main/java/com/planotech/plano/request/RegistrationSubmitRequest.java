package com.planotech.plano.request;

import lombok.Data;

import java.util.Map;

@Data
public class RegistrationSubmitRequest {

    private String name;
    private String email;
    private String phone;

    // key = fieldKey, value = user input
    private Map<String, Object> responses;
}