package com.planotech.plano.model;

import com.planotech.plano.enums.FormSectionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long formSectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private RegistrationForm form;

    @Enumerated(EnumType.STRING)
    private FormSectionType type;

    @Column(columnDefinition = "JSON")
    private String dataJson;

    private Integer displayOrder;

    private Boolean active = true;
}