package com.planotech.plano.service;

import com.planotech.plano.exception.ResourceNotFoundException;
import com.planotech.plano.model.EmailContentSettings;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.EventUser;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.EmailContentSettingsRepository;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.request.EmailContentRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EventRepository eventRepository;
    private final EmailContentSettingsRepository emailContentSettingsRepository;
    private final EventAuthorizationService eventAuthorizationService;

    @Transactional
    public ResponseEntity<?> updateEmailContent(
            Long eventId,
            EmailContentRequest request,
            User user
    ) {
        EventUser eu = eventAuthorizationService.authorize(eventId, user);
        eventAuthorizationService.validateDraftPermission(user, eu);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found"));

        EmailContentSettings settings =
                emailContentSettingsRepository
                        .findByEvent_EventId(eventId)
                        .orElseGet(() -> {
                            EmailContentSettings newSettings =
                                    new EmailContentSettings();
                            newSettings.setEvent(event);
                            return newSettings;
                        });

        if (request.getEventName() != null) event.setName(request.getEventName());
        if (request.getEventDescription() != null) event.setDescription(request.getEventDescription());
        if (request.getEventLocation() != null)
            event.setLocation(request.getEventLocation());
        if (request.getEventDate() != null) event.setStartDate(LocalDate.parse(request.getEventDate()));
        if (request.getEventLogo() != null) event.setLogoUrl(request.getEventLogo());

        eventRepository.save(event);
        settings.setSupportEmail(request.getSupportEmail());
        settings.setSupportPhone(request.getSupportPhone());
        settings.setEventWebsite(request.getEventWebsite());
        settings.setUpdatedAt(LocalDateTime.now());

        emailContentSettingsRepository.save(settings);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Email content updated successfully"
        ));
    }

    @Transactional
    public ResponseEntity<?> getEmailTemplate(Long eventId, User user) {

        eventAuthorizationService.authorize(eventId, user);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        EmailContentSettings template = emailContentSettingsRepository.findByEvent_EventId(eventId)
                .orElse(null);

        Map<String, Object> response = new HashMap<>();

        response.put("eventName", event.getName());
        response.put("eventDescription", event.getDescription());
        response.put("eventDate", event.getStartDate());
        response.put("eventLocation", event.getLocation());
        response.put("eventLogo", event.getLogoUrl());


        response.put("eventWebsite",
                template != null ? template.getEventWebsite() : null);

        response.put("supportEmail",
                template != null ? template.getSupportEmail() : null);

        response.put("supportPhone",
                template != null ? template.getSupportPhone() : null);

        response.put("username", "USERNAME");
        response.put("userEmail", "USER@EMAIL.COM");
        response.put("qrUrl", "https://aws.quantumparadigm.in/documents/HIPC_EVT-859aa97f-f7a8-4f19-9805-19e88ee9a96c/qr_1cdd3e57.png");
        response.put("bodgeCode", "BDG-00F2CD52");

        return ResponseEntity.ok(response);
    }
}
