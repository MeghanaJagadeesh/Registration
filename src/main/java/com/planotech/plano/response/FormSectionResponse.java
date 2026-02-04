package com.planotech.plano.response;

import com.planotech.plano.enums.FormSectionType;
import lombok.Data;

@Data
public class FormSectionResponse {

    private Long formSectionId;
    private FormSectionType type;
    private String dataJson;
    private Integer displayOrder;
}