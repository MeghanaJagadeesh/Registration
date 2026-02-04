package com.planotech.plano.repository;

import com.planotech.plano.enums.FormSectionType;
import com.planotech.plano.model.FormSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormSectionRepository extends JpaRepository<FormSection, Long> {
    List<FormSection> findByForm_FormIdOrderByDisplayOrderAsc(Long formId);

    Optional<FormSection> findByForm_FormIdAndTypeAndActiveTrue(
            Long formId,
            FormSectionType type
    );

    List<FormSection> findByForm_FormIdAndActiveTrueOrderByDisplayOrder(
            Long formId
    );
}

