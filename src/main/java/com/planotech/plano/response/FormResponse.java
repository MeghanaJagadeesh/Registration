package com.planotech.plano.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormResponse {
    private Long formId;
    private Integer version;
}

