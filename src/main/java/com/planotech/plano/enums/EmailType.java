package com.planotech.plano.enums;

import lombok.Getter;

@Getter
public enum EmailType {

    FORGOT_PASSWORD(
            "Password Reset Request",
            "password_reset_request.html"
    ),
    EVENT_REGISTRATION_CONFIRMATION(
        "Registration Confirmed",
                "event-registration-confirmation.html"
    );

    private final String subject;
    private final String template;

    EmailType(String subject, String template) {
        this.subject = subject;
        this.template = template;
    }

}

