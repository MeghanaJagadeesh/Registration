package com.planotech.plano.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmailContentRequest {

    private String eventName;
    private String eventDescription;
    private String eventLocation;
    private String eventDate;
    private String supportEmail;
    private String supportPhone;
    private String eventWebsite;
    private String eventLogo;
    private String eventStartTime;
}

