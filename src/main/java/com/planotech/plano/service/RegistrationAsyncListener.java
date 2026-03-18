package com.planotech.plano.service;

import com.planotech.plano.enums.EmailType;
import com.planotech.plano.helper.EmailSender;
import com.planotech.plano.helper.FileStorageService;
import com.planotech.plano.helper.QRCodeGenerator;
import com.planotech.plano.model.*;
import com.planotech.plano.record.RegistrationCompletedEvent;
import com.planotech.plano.repository.EmailContentSettingsRepository;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.RegistrationEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegistrationAsyncListener {

    private final RegistrationEntryRepository entryRepository;
    private final EventRepository eventRepository;
    private final EmailSender emailSender;
    private final RegistrationEmailVariableService variableService;
    private final QRCodeGenerator qrCodeGenerator;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final EmailContentSettingsRepository emailContentSettingsRepository;

    @Async("backgroundExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRegistrationCompleted(RegistrationCompletedEvent eventData) {

        try {
            RegistrationEntry entry = entryRepository.findById(eventData.entryId())
                    .orElseThrow(() ->
                            new IllegalStateException("RegistrationEntry not found: " + eventData.entryId())
                    );

            Event event = eventRepository.findById(eventData.eventId())
                    .orElseThrow(() ->
                            new IllegalStateException("Event not found: " + eventData.eventId())
                    );

            RegistrationForm form = entry.getForm();

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("name", entry.getName());
            payloadMap.put("email", entry.getEmail());
            payloadMap.put("badge", entry.getBadgeCode());
            payloadMap.put("entryId", entry.getEntryId());
            payloadMap.put("event", event.getName());

            String qrPayload = objectMapper.writeValueAsString(payloadMap);

            byte[] image = qrCodeGenerator.generateQrCodeImage(qrPayload);
            Map<String, byte[]> inlineImages = new HashMap<>();
            inlineImages.put("qrImage", image);

            if (event.getLogoUrl() != null) {
                byte[] logoBytes = getImageBytesFromUrl(event.getLogoUrl());
                inlineImages.put("eventLogo", logoBytes);
            }

            entryRepository.save(entry);

            User tempUser = new User();
            tempUser.setEmail(entry.getEmail());
            tempUser.setName(entry.getName());

            Map<String, Object> variable = variableService.buildRegistrationEmailVariables(event, form, entry);
            emailSender.sendVerificationEmail(
                    tempUser,
                    EmailType.EVENT_REGISTRATION_CONFIRMATION,
                    variable,
                    inlineImages
            );

        } catch (Exception e) {
            log.error("Registration async processing failed", e);
        }
    }

    private byte[] getImageBytesFromUrl(String imageUrl) {
        System.out.println(imageUrl);
        try {
            URI uri = new URI(imageUrl.replace(" ", "%20"));
            URL url = uri.toURL();

            try (InputStream in = url.openStream()) {
                return in.readAllBytes();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load image from URL", e);
        }
    }
}

