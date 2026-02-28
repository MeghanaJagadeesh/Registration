package com.planotech.plano.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BadgeConfigRequestDTO {
    private List<String> selectedFieldKeys;
}
