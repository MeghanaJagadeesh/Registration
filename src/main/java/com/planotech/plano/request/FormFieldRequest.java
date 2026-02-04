package com.planotech.plano.request;

import com.planotech.plano.enums.FieldType;
import lombok.Data;

@Data
public class FormFieldRequest {
    private Long id;
    private String fieldKey;
    private String label;
    private FieldType fieldType;
    private boolean required;
    private Integer displayOrder;
    private String optionsJson;
    private boolean deleted;
}