package com.planotech.plano.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BadgeFormField {
    private String fieldKey;
    private String label;
    private Boolean required;
    private String fieldType;
}
