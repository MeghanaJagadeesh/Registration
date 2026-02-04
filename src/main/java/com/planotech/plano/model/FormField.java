package com.planotech.plano.model;

import com.planotech.plano.enums.FieldType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long formFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private RegistrationForm form;

    // Unique key used in JSON responses
    @Column(nullable = false)
    private String fieldKey;   // ex: "designation", "profile_photo"

    private String label;      // UI label

    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    private Boolean required = false;

    private Integer displayOrder;

    // For dropdown / radio / checkbox
    @Column(columnDefinition = "TEXT")
    private String optionsJson;
}

