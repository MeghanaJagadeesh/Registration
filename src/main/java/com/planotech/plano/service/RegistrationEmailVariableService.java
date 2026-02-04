package com.planotech.plano.service;

import com.planotech.plano.helper.FileStorageService;
import com.planotech.plano.helper.QRCodeGenerator;
import com.planotech.plano.model.Event;
import com.planotech.plano.model.RegistrationEntry;
import com.planotech.plano.model.RegistrationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Service
public class RegistrationEmailVariableService {

    @Autowired
    FormSectionService formSectionService;

    @Autowired
    QRCodeGenerator qrCodeGenerator;

    @Autowired
    FileStorageService fileStorageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> buildRegistrationEmailVariables(
            Event event,
            RegistrationForm form,
            RegistrationEntry entry
    ) {

        Map<String, Object> vars = new HashMap<>();

        vars.put("userName", entry.getName());
        vars.put("userEmail", entry.getEmail());

        vars.put("eventName", event.getName());
        vars.put("eventDescription", event.getDescription());
        vars.put("eventLocation", event.getLocation());
        vars.put("badgeCode", entry.getBadgeCode());
        vars.put("eventStartDate",
                event.getStartDate() != null
                        ? event.getStartDate().toString()
                        : ""
        );

        vars.put(
                "eventLogo",
                event.getLogoUrl() != null
                        ? event.getLogoUrl()
                        : "https://default-logo.png"
        );

        vars.put("registrationCode", entry.getBadgeCode());
        Map<String, String> mailVars =
                formSectionService.getMailVariables(form.getFormId());

//        String qrData = entry.getName() + "|" + entry.getEmail() + "|" + entry.getBadgeCode();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("name", entry.getName());
        payloadMap.put("email", entry.getEmail());
        payloadMap.put("badge", entry.getBadgeCode());
        payloadMap.put("entryId", entry.getEntryId());
        payloadMap.put("event", event.getName());

        String qrPayload = objectMapper.writeValueAsString(payloadMap);

        BufferedImage image = qrCodeGenerator.generateQrCodeImage(qrPayload);
        String qrUrl = fileStorageService.handleFileUploadAsync(image, event.getName(), event.getEventKey()).join();
        vars.put("qrUrl", qrUrl);

        mailVars.putIfAbsent("eventWebsite", "#");
        mailVars.putIfAbsent("supportEmail", "support@" + event.getEventKey().toLowerCase() + ".com");
        mailVars.putIfAbsent("supportPhone", "");
        mailVars.putIfAbsent("eventTime", "To be announced");

        // Footer logo fallback
        if (!mailVars.containsKey("footerLogo")
                || mailVars.get("footerLogo") == null
                || mailVars.get("footerLogo").isBlank()) {

            mailVars.put("footerLogo",
                    event.getLogoUrl() != null
                            ? event.getLogoUrl()
                            : "https://default-logo.png"
            );
        }

        vars.putAll(mailVars);

        return vars;
    }
}

