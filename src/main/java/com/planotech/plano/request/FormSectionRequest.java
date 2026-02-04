package com.planotech.plano.request;

import com.planotech.plano.enums.FormSectionType;
import lombok.Data;

@Data
public class FormSectionRequest {
    private Long id;
    private FormSectionType type;
    private String dataJson;
    private Integer displayOrder;
    private Boolean deleted = false;
}