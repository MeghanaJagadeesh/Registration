package com.planotech.plano.service;

import com.planotech.plano.enums.EmailType;
import com.planotech.plano.helper.EmailSender;
import com.planotech.plano.helper.FileStorageService;
import com.planotech.plano.helper.QRCodeGenerator;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.RegistrationEntry;
import com.planotech.plano.model.RegistrationForm;
import com.planotech.plano.model.User;
import com.planotech.plano.record.RegistrationCompletedEvent;
import com.planotech.plano.repository.EventRepository;
import com.planotech.plano.repository.RegistrationEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
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

//    @Async("backgroundExecutor")
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleRegistrationCompleted(
//            RegistrationCompletedEvent eventData) {
//
//        RegistrationEntry entry =
//                entryRepository.findById(eventData.entryId())
//                        .orElseThrow();
//
//        Event event =
//                eventRepository.findById(eventData.eventId())
//                        .orElseThrow();
//
//        RegistrationForm form = entry.getForm();
//
//        try {
//            String qrPayload = String.format(
//                    "Name: %s\nEmail: %s\nBadge: %s\nEvent: %s",
//                    entry.getName(),
//                    entry.getEmail(),
//                    entry.getBadgeCode(),
//                    event.getName()
//            );
//
//            BufferedImage image = qrCodeGenerator.generateQrCodeImage(qrPayload);
//            String qrUrl = fileStorageService.handleFileUploadAsync(image, event.getName(), event.getEventKey()).join();
//            entry.setQrUrl(qrUrl);
//            entryRepository.save(entry);
//
//            User tempUser = new User();
//            tempUser.setEmail(entry.getEmail());
//            tempUser.setName(entry.getName());
//
//            emailSender.sendVerificationEmail(
//                    tempUser,
//                    EmailType.EVENT_REGISTRATION_CONFIRMATION,
//                    variableService.buildRegistrationEmailVariables(
//                            event, form, entry
//                    )
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Something went wrong " + e.getMessage());
//        }
//    }

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

            BufferedImage image = qrCodeGenerator.generateQrCodeImage(qrPayload);
            String qrUrl = fileStorageService
                    .handleFileUploadAsync(image, event.getName(), event.getEventKey())
                    .join();

            entry.setQrUrl(qrUrl);
            entryRepository.save(entry);

            User tempUser = new User();
            tempUser.setEmail(entry.getEmail());
            tempUser.setName(entry.getName());

            emailSender.sendVerificationEmail(
                    tempUser,
                    EmailType.EVENT_REGISTRATION_CONFIRMATION,
                    variableService.buildRegistrationEmailVariables(event, form, entry)
            );

        } catch (Exception e) {
            log.error("Registration async processing failed", e);
        }
    }

}

