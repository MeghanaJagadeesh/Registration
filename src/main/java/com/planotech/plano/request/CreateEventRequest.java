package com.planotech.plano.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@Data
public class CreateEventRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String logo;
    private String location;

    private String username;
    private String email;
    private String phone;
    private String password;
}
