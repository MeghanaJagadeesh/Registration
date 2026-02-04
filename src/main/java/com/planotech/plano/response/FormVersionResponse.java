package com.planotech.plano.response;

import com.planotech.plano.enums.FormStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FormVersionResponse {
    private Long formId;
    private Integer version;
    private FormStatus status;
    private Boolean active;
    private LocalDateTime publishedAt;
}
