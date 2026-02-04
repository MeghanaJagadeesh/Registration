package com.planotech.plano.response;

import com.planotech.plano.enums.FieldType;
import lombok.Data;

@Data
public class FormFieldResponse {
    private Long formFieldId;
    private String fieldKey;
    private String label;
    private FieldType fieldType;
    private Boolean required;
    private Integer displayOrder;
    private String optionsJson;
}
