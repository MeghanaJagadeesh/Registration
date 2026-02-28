package com.planotech.plano.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RegistrationTrendDTO {
    private LocalDate date;
    private long registrations;
}
