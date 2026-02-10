package com.planotech.plano.service;

import com.planotech.plano.enums.FormSectionType;
import com.planotech.plano.enums.FormStatus;
import com.planotech.plano.exception.CustomBadRequestException;
import com.planotech.plano.exception.ResourceNotFoundException;
import com.planotech.plano.helper.JsonUtil;
import com.planotech.plano.model.EventUser;
import com.planotech.plano.model.FormSection;
import com.planotech.plano.model.RegistrationForm;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.FormSectionRepository;
import com.planotech.plano.repository.RegistrationFormRepository;
import com.planotech.plano.request.FormSectionRequest;
import com.planotech.plano.response.FormSectionResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FormSectionService {

    @Autowired
    RegistrationFormRepository formRepository;

    @Autowired
    FormSectionRepository sectionRepository;

    @Autowired
    EventAuthorizationService eventAuthorizationService;

    @Autowired
    JsonUtil jsonUtil;

    @Transactional
    public void saveSections(
            Long formId,
            List<FormSectionRequest> requests,
            User user
    ) {

        RegistrationForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found"));

        EventUser eu = eventAuthorizationService.authorize(form.getEvent().getEventId(), user);
        eventAuthorizationService.validateDraftPermission(user, eu);
        if (form.getStatus() != FormStatus.DRAFT) {
            throw new CustomBadRequestException("Cannot edit published form");
        }

        List<FormSection> existingSections =
                sectionRepository.findByForm_FormIdOrderByDisplayOrderAsc(formId);

        Map<Long, FormSection> existingMap =
                existingSections
                        .stream()
                        .collect(Collectors.toMap(FormSection::getFormSectionId, s -> s));
        Set<Long> incomingIds = new HashSet<>();

        List<FormSection> toSave = new ArrayList<>();

        for (FormSectionRequest req : requests) {

            FormSection section;

            if (req.getId() != null && existingMap.containsKey(req.getId())) {
                section = existingMap.get(req.getId());
                incomingIds.add(req.getId());
            } else {
                section = new FormSection();
                section.setForm(form);
            }

            section.setType(req.getType());
            section.setDataJson(req.getDataJson());
            section.setDisplayOrder(req.getDisplayOrder());
            section.setActive(true);

            toSave.add(section);
        }

        // 🔥 delete only removed sections
        for (FormSection existing : existingSections) {
            if (!incomingIds.contains(existing.getFormSectionId())) {
                sectionRepository.delete(existing);
            }
        }

        sectionRepository.saveAll(toSave);
    }

    @Transactional
    public List<FormSectionResponse> getSections(Long formId, User user) {
        RegistrationForm form = formRepository.findById(formId).orElseThrow(() -> new ResourceNotFoundException("Form Not FOund"));
        EventUser eu = eventAuthorizationService.authorize(form.getEvent().getEventId(), user);
        eventAuthorizationService.validateDraftPermission(user, eu);

        return sectionRepository.findByForm_FormIdOrderByDisplayOrderAsc(formId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private FormSectionResponse toDto(FormSection section) {
        FormSectionResponse r = new FormSectionResponse();
        r.setFormSectionId(section.getFormSectionId());
        r.setType(section.getType());
        r.setDataJson(section.getDataJson());
        r.setDisplayOrder(section.getDisplayOrder());
        return r;
    }

    public Map<String, Object> getMailVariables(Long formId) {

        FormSection mailSection = sectionRepository
                .findByForm_FormIdAndTypeAndActiveTrue(
                        formId,
                        FormSectionType.FORM_MAIL
                )
                .orElse(null);

        if (mailSection == null) {
            return new HashMap<>();
        }

        return jsonUtil.toMap(mailSection.getDataJson());
    }
}
