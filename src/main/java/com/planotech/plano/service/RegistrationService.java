package com.planotech.plano.service;

import com.planotech.plano.record.RegistrationCompletedEvent;
import com.planotech.plano.enums.FormStatus;
import com.planotech.plano.exception.ResourceNotFoundException;
import com.planotech.plano.model.*;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.FormSectionRepository;
import com.planotech.plano.repository.RegistrationEntryRepository;
import com.planotech.plano.repository.RegistrationFormRepository;
import com.planotech.plano.request.RegistrationSubmitRequest;
import com.planotech.plano.response.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RegistrationService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationFormRepository formRepository;

    @Autowired
    private RegistrationEntryRepository entryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    FormSectionRepository sectionRepository;

    @Autowired
    EventAuthorizationService eventAuthorizationService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public ResponseEntity<?> register(String eventKey,
                                      RegistrationSubmitRequest request
    ) {

        Event event = eventRepository.findByEventKey(eventKey)
                .orElseThrow(() -> new ResourceNotFoundException("Something went wrong! Event not found"));

        RegistrationForm form = formRepository
                .findTopByEvent_EventIdAndStatusOrderByVersionDesc(event.getEventId(), FormStatus.PUBLISHED)
                .orElseThrow(() -> new IllegalStateException("Registration not open"));

        boolean alreadyRegistered = entryRepository
                .existsByEvent_EventIdAndEmail(event.getEventId(), request.getEmail());

        if (alreadyRegistered) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "status", "failed",
                            "message", "You have already registered for this event",
                            "code", HttpStatus.CONFLICT
                    ));
        }
        validateRequiredFields(form, request);

        RegistrationEntry entry = new RegistrationEntry();
        entry.setEvent(event);
        entry.setForm(form);
        entry.setName(request.getName());
        entry.setEmail(request.getEmail());
        entry.setPhone(request.getPhone());
        entry.setSubmittedAt(LocalDateTime.now());

        try {
            entry.setResponsesJson(
                    objectMapper.writeValueAsString(request.getResponses())
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid response data");
        }

        entryRepository.save(entry);

//        User tempUser = new User();
//        tempUser.setEmail(entry.getEmail());
//        tempUser.setName(entry.getName());
//
//        emailSender.sendVerificationEmail(
//                tempUser,
//                EmailType.EVENT_REGISTRATION_CONFIRMATION,
//                registrationEmailVariableService.buildRegistrationEmailVariables(event, form, entry)
//        );

        eventPublisher.publishEvent(
                new RegistrationCompletedEvent(
                        entry.getEntryId(),
                        event.getEventId()
                )
        );
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Registration successful",
                "registrationId", entry.getEntryId()
        ));
    }

    private void validateRequiredFields(
            RegistrationForm form,
            RegistrationSubmitRequest request
    ) {

        if (request.getName() == null || request.getEmail() == null) {
            throw new IllegalArgumentException("Name and Email are required");
        }

        Map<String, Object> responses =
                Optional.ofNullable(request.getResponses())
                        .orElse(Collections.emptyMap());

        for (FormField field : form.getFields()) {
            if (Boolean.TRUE.equals(field.getRequired())) {
                Object value = responses.get(field.getFieldKey());
                if (value == null || value.toString().isBlank()) {
                    throw new IllegalArgumentException(
                            "Required field missing: " + field.getLabel()
                    );
                }
            }
        }
    }

    public ResponseEntity<?> getLiveForm(String eventKey) {

        Event event = eventRepository.findByEventKey(eventKey)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        RegistrationForm form = formRepository
                .findTopByEvent_EventIdAndStatusOrderByVersionDesc(
                        event.getEventId(),
                        FormStatus.PUBLISHED
                )
                .orElseThrow(() -> new IllegalStateException("Registration is not open"));

        List<FormSectionResponse> sections =
                sectionRepository.findByForm_FormIdOrderByDisplayOrderAsc(form.getFormId())
                        .stream()
                        .map(this::toDto)
                        .toList();

        List<FormFieldResponse> fields =
                form.getFields().stream()
                        .sorted(Comparator.comparing(FormField::getDisplayOrder))
                        .map(this::toFieldDto)
                        .toList();

        User user = event.getCreatedBy();
        UserDTO userDTO = new UserDTO(user.getUserId(), user.getName(), user.getEmail(), null);
        PublicFormResponse response = new PublicFormResponse(
                eventKey,
                new FormResponse(
                        form.getFormId(),
                        form.getVersion()
                ),
                sections,
                fields
        );

        return ResponseEntity.ok(response);
    }


    private FormSectionResponse toDto(FormSection section) {
        FormSectionResponse r = new FormSectionResponse();
        r.setFormSectionId(section.getFormSectionId());
        r.setType(section.getType());
        r.setDataJson(section.getDataJson());
        r.setDisplayOrder(section.getDisplayOrder());
        return r;
    }

    private FormFieldResponse toFieldDto(FormField f) {
        FormFieldResponse r = new FormFieldResponse();
        r.setFormFieldId(f.getFormFieldId());
        r.setFieldKey(f.getFieldKey());
        r.setLabel(f.getLabel());
        r.setFieldType(f.getFieldType());
        r.setRequired(f.getRequired());
        r.setDisplayOrder(f.getDisplayOrder());
        r.setOptionsJson(f.getOptionsJson());
        return r;
    }

    @Transactional
    public ResponseEntity<?> getRegistrations(
            Long eventId,
            int page,
            int size,
            String search,
            User user
    ) {
        eventAuthorizationService.authorize(eventId, user);
        page = Math.max(page, 0);
        size = Math.min(size, 100);
        search = (search == null) ? null : search.trim();
        Pageable pageable = PageRequest.of(
                page, size, Sort.by("submittedAt").descending()
        );

        Page<RegistrationEntry> pageResult;

        if (search != null && !search.isBlank()) {
            pageResult = entryRepository.search(eventId, search, pageable);
        } else {
            pageResult = entryRepository.findByEvent_EventId(eventId, pageable);
        }

        List<RegistrationAdminResponse> data = pageResult.getContent()
                .stream()
                .map(this::toAdminResponse)
                .toList();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "totalElements", pageResult.getTotalElements(),
                "totalPages", pageResult.getTotalPages(),
                "data", data
        ));
    }

    private RegistrationAdminResponse toAdminResponse(RegistrationEntry entry) {

        RegistrationAdminResponse res = new RegistrationAdminResponse();
        res.setRegistrationId(entry.getEntryId());
        res.setName(entry.getName());
        res.setEmail(entry.getEmail());
        res.setPhone(entry.getPhone());
        res.setSubmittedAt(entry.getSubmittedAt());
        res.setCheckedIn(entry.getCheckedIn());

        try {
            res.setResponses(
                    objectMapper.readValue(
                            entry.getResponsesJson(),
                            new TypeReference<Map<String, Object>>() {
                            }
                    )
            );
        } catch (Exception e) {
            res.setResponses(Map.of());
        }
        return res;
    }

}
